package com.academia.backend.service;

import com.academia.backend.model.Aluno;
import com.academia.backend.model.CheckIn;
import com.academia.backend.observer.CheckInObserver;
import com.academia.backend.repository.memory.InMemoryAlunoRepository;
import com.academia.backend.repository.memory.InMemoryCheckInRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class CheckInServiceTest {

    private InMemoryAlunoRepository alunoRepository;
    private InMemoryCheckInRepository checkInRepository;
    private AlunoService alunoService;

    @BeforeEach
    void configurar() {
        alunoRepository = new InMemoryAlunoRepository();
        checkInRepository = new InMemoryCheckInRepository();
        alunoService = new AlunoService(alunoRepository);
    }

    @Test
    void deveRegistrarCheckInENotificarObservadores() {
        Aluno aluno = alunoService.cadastrar("Ana Souza");
        AtomicInteger notificacoes = new AtomicInteger();

        CheckInObserver observer = checkIn -> notificacoes.incrementAndGet();
        CheckInService service = new CheckInService(
                alunoService, checkInRepository, List.of(observer)
        );

        CheckIn checkIn = service.realizarCheckIn(aluno.id());

        assertNotNull(checkIn.id());
        assertEquals(aluno.id(), checkIn.alunoId());
        assertEquals(1, service.listarPorAluno(aluno.id()).size());
        assertEquals(1, notificacoes.get());
    }

    @Test
    void deveRejeitarCheckInDeAlunoInexistente() {
        CheckInService service = new CheckInService(
                alunoService, checkInRepository, List.of()
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.realizarCheckIn(UUID.randomUUID())
        );
    }

    @Test
    void falhaEmUmObserverNaoDeveImpedirOsDemais() {
        Aluno aluno = alunoService.cadastrar("Ana Souza");
        AtomicInteger segundoObserverExecutado = new AtomicInteger();

        CheckInObserver observerComFalha = checkIn -> {
            throw new IllegalStateException("Falha simulada");
        };
        CheckInObserver segundoObserver = checkIn -> segundoObserverExecutado.incrementAndGet();

        CheckInService service = new CheckInService(
                alunoService,
                checkInRepository,
                List.of(observerComFalha, segundoObserver)
        );

        assertDoesNotThrow(() -> service.realizarCheckIn(aluno.id()));
        assertEquals(1, segundoObserverExecutado.get());
        assertEquals(1, checkInRepository.listarPorAlunoId(aluno.id()).size());
    }

    @Test
    void observerRegistradoDepoisDeveSerNotificado() {
        Aluno aluno = alunoService.cadastrar("Ana Souza");
        AtomicInteger notificacoes = new AtomicInteger();
        CheckInService service = new CheckInService(
                alunoService, checkInRepository, List.of()
        );

        service.registrarObserver(checkIn -> notificacoes.incrementAndGet());
        service.realizarCheckIn(aluno.id());

        assertEquals(1, notificacoes.get());
    }

    @Test
    void observerRemovidoNaoDeveMaisSerNotificado() {
        Aluno aluno = alunoService.cadastrar("Ana Souza");
        AtomicInteger notificacoes = new AtomicInteger();
        CheckInObserver observer = checkIn -> notificacoes.incrementAndGet();
        CheckInService service = new CheckInService(
                alunoService, checkInRepository, List.of(observer)
        );

        service.realizarCheckIn(aluno.id());
        service.removerObserver(observer);
        service.realizarCheckIn(aluno.id());

        assertEquals(1, notificacoes.get());
    }
}
