package com.academia.backend.client;

import com.academia.backend.model.Aluno;
import com.academia.backend.model.Exercicio;
import com.academia.backend.model.PlanoTreino;
import com.academia.backend.model.enums.GrupoMuscular;
import com.academia.backend.observer.impl.EstatisticaFrequenciaObserver;
import com.academia.backend.observer.impl.NotificacaoObserver;
import com.academia.backend.service.AlunoService;
import com.academia.backend.service.CheckInService;
import com.academia.backend.service.PlanoTreinoService;
import org.springframework.stereotype.Component;

@Component
public class AcademiaClient {

    private final AlunoService alunoService;
    private final PlanoTreinoService planoTreinoService;
    private final CheckInService checkInService;
    private final EstatisticaFrequenciaObserver estatisticaObserver;
    private final NotificacaoObserver notificacaoObserver;

    public AcademiaClient(
            AlunoService alunoService,
            PlanoTreinoService planoTreinoService,
            CheckInService checkInService,
            EstatisticaFrequenciaObserver estatisticaObserver,
            NotificacaoObserver notificacaoObserver
    ) {
        this.alunoService = alunoService;
        this.planoTreinoService = planoTreinoService;
        this.checkInService = checkInService;
        this.estatisticaObserver = estatisticaObserver;
        this.notificacaoObserver = notificacaoObserver;
    }

    /** Executa um pequeno cenário para demonstrar o funcionamento do backend. */
    public void executar() {
        System.out.println();
        System.out.println("====================================================");
        System.out.println("       SISTEMA DE ACADEMIA - DEMONSTRAÇÃO");
        System.out.println("====================================================");

        Aluno aluno = alunoService.cadastrar("Ana Souza");
        System.out.printf("[CLIENT] Aluna cadastrada: %s (ID: %s)%n", aluno.nome(), aluno.id());

        // Builder: os parâmetros obrigatórios são informados no início e os opcionais
        // são configurados de forma encadeada.
        PlanoTreino plano = PlanoTreino.builder(
                        aluno.id(),
                        "Treino A",
                        "Condicionamento físico"
                )
                .frequenciaSemanal(3)
                .adicionarExercicio(new Exercicio(
                        "Agachamento livre", GrupoMuscular.PERNAS, 3, 12
                ))
                .adicionarExercicio(new Exercicio(
                        "Supino com halteres", GrupoMuscular.PEITO, 3, 10
                ))
                .adicionarExercicio(new Exercicio(
                        "Remada baixa", GrupoMuscular.COSTAS, 3, 12
                ))
                .observacoes("Priorizar a execução correta dos movimentos.")
                .build();

        planoTreinoService.salvar(plano);
        System.out.printf("%n[CLIENT] Plano '%s' criado para %s.%n", plano.getNome(), aluno.nome());
        System.out.printf("[CLIENT] Frequência: %d dias por semana.%n", plano.getFrequenciaSemanal());
        plano.getExercicios().forEach(exercicio -> System.out.printf(
                "         - %s (%s): %d séries de %d repetições%n",
                exercicio.nome(), exercicio.grupoMuscular(),
                exercicio.series(), exercicio.repeticoes()
        ));

        System.out.println("\n[CLIENT] Realizando dois check-ins para demonstrar o Observer...");
        checkInService.realizarCheckIn(aluno.id());
        checkInService.realizarCheckIn(aluno.id());

        // Observer dinâmico: removemos um observador e o terceiro check-in não o notifica.
        System.out.println("\n[CLIENT] Removendo o observador de notificação e fazendo um terceiro check-in...");
        checkInService.removerObserver(notificacaoObserver);
        checkInService.realizarCheckIn(aluno.id());
        checkInService.registrarObserver(notificacaoObserver);

        System.out.printf(
                "\n[CLIENT] Check-ins registrados: %d%n",
                checkInService.listarPorAluno(aluno.id()).size()
        );
        System.out.printf(
                "[CLIENT] Total contabilizado pelo observador de estatísticas: %d%n",
                estatisticaObserver.consultarTotal(aluno.id())
        );

        System.out.println("\n====================================================");
        System.out.println("            DEMONSTRAÇÃO FINALIZADA");
        System.out.println("====================================================");
    }
}
