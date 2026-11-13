package com.academia.backend.service;

import com.academia.backend.model.Aluno;
import com.academia.backend.model.CheckIn;
import com.academia.backend.observer.CheckInObserver;
import com.academia.backend.repository.CheckInRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Serviço de check-in. Também exerce o papel de Subject (observável) do padrão Observer:
 * mantém a lista de observadores e os notifica sempre que um check-in é registrado.
 */
@Service
public class CheckInService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CheckInService.class);

    private final AlunoService alunoService;
    private final CheckInRepository checkInRepository;
    // CopyOnWriteArrayList permite registrar/remover observadores sem quebrar uma notificação em andamento.
    private final List<CheckInObserver> observers = new CopyOnWriteArrayList<>();

    public CheckInService(
            AlunoService alunoService,
            CheckInRepository checkInRepository,
            List<CheckInObserver> observers
    ) {
        this.alunoService = alunoService;
        this.checkInRepository = checkInRepository;
        this.observers.addAll(observers);
    }

    /** Inscreve um observador para ser avisado nos próximos check-ins. */
    public void registrarObserver(CheckInObserver observer) {
        Objects.requireNonNull(observer, "O observer não pode ser nulo.");
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    /** Cancela a inscrição de um observador. */
    public void removerObserver(CheckInObserver observer) {
        observers.remove(observer);
    }

    public CheckIn realizarCheckIn(UUID alunoId) {
        Aluno aluno = alunoService.buscarAtivo(alunoId);
        CheckIn checkIn = checkInRepository.salvar(CheckIn.novo(aluno.id()));

        notificarObservadores(checkIn);
        return checkIn;
    }

    public List<CheckIn> listarPorAluno(UUID alunoId) {
        alunoService.buscarAtivo(alunoId);
        return checkInRepository.listarPorAlunoId(alunoId);
    }

    private void notificarObservadores(CheckIn checkIn) {
        for (CheckInObserver observer : observers) {
            try {
                observer.aoRealizarCheckIn(checkIn);
            } catch (RuntimeException exception) {
                // Uma falha em um efeito secundário não desfaz o check-in já registrado
                // nem impede a execução dos demais observadores.
                LOGGER.error(
                        "Falha ao notificar o observador {} para o check-in {}",
                        observer.getClass().getSimpleName(),
                        checkIn.id(),
                        exception
                );
            }
        }
    }
}
