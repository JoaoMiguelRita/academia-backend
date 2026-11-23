package com.academia.backend.observer.impl;

import com.academia.backend.model.CheckIn;
import com.academia.backend.model.PlanoTreino;
import com.academia.backend.observer.CheckInObserver;
import com.academia.backend.repository.CheckInRepository;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class MetaSemanalObserver implements CheckInObserver {

    private static final ZoneId FUSO_HORARIO = ZoneId.of("America/Sao_Paulo");

    private final CheckInRepository checkInRepository;
    private final ConcurrentMap<UUID, PlanoTreino> planosAcompanhados = new ConcurrentHashMap<>();
    private final Set<MetaNotificada> metasNotificadas = ConcurrentHashMap.newKeySet();

    public MetaSemanalObserver(CheckInRepository checkInRepository) {
        this.checkInRepository = checkInRepository;
    }

    /** Define o plano que será acompanhado nos próximos check-ins do aluno. */
    public void acompanharPlano(PlanoTreino plano) {
        Objects.requireNonNull(plano, "O plano acompanhado é obrigatório.");
        planosAcompanhados.put(plano.getAlunoId(), plano);
    }

    @Override
    public void aoRealizarCheckIn(CheckIn checkIn) {
        PlanoTreino plano = planosAcompanhados.get(checkIn.alunoId());
        if (plano == null) {
            return;
        }

        LocalDate data = checkIn.horario().atZone(FUSO_HORARIO).toLocalDate();
        LocalDate inicioPlano = plano.getDataCriacao();
        LocalDate fimPlano = inicioPlano.plusWeeks(plano.getDuracaoSemanas());
        if (data.isBefore(inicioPlano) || !data.isBefore(fimPlano)) {
            return;
        }

        LocalDate inicioSemana = data.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate fimSemana = inicioSemana.plusWeeks(1);
        long diasTreinados = checkInRepository.listarPorAlunoId(checkIn.alunoId()).stream()
                .map(presenca -> presenca.horario().atZone(FUSO_HORARIO).toLocalDate())
                .filter(dia -> !dia.isBefore(inicioSemana) && dia.isBefore(fimSemana))
                .filter(dia -> !dia.isBefore(inicioPlano) && dia.isBefore(fimPlano))
                .distinct()
                .count();

        MetaNotificada meta = new MetaNotificada(plano.getId(), inicioSemana);
        if (diasTreinados >= plano.getFrequenciaSemanal() && metasNotificadas.add(meta)) {
            System.out.printf(
                    "[META SEMANAL] Aluno %s atingiu a meta de %d dia(s) na semana de %s com o plano '%s'.%n",
                    checkIn.alunoId(), plano.getFrequenciaSemanal(), inicioSemana, plano.getNome()
            );
        }
    }

    private record MetaNotificada(UUID planoId, LocalDate inicioSemana) {
    }
}
