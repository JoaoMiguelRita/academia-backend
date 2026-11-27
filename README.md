# Sistema de Academia — Backend Java

Aplicação de console em Java 21 e Spring Boot para o Trabalho Final de Design Patterns (Parte 1), tema **Academia**. O foco são os padrões **Builder** e **Observer**.

## Descrição do sistema

Gerenciamento simplificado de uma academia, com três entidades principais:

| Entidade | Descrição |
|---|---|
| `Aluno` | Pessoa cadastrada na academia (ativa ou inativa). |
| `PlanoTreino` | Plano de treino do aluno: nome, objetivo, frequência semanal, exercícios e observações. |
| `CheckIn` | Registro de presença do aluno, com data e hora. |

Os dados ficam apenas em memória (sem banco de dados). Ao iniciar, a `AcademiaApplication` executa um cenário de demonstração no console (`AcademiaClient`).

## Tecnologias e pré-requisitos

- Java (JDK) 21 ou superior
- Maven 3.6.3 ou superior
- Spring Boot e JUnit 5

## Como executar

```bash
mvn spring-boot:run   # executa a demonstração
mvn test              # executa os testes
```

## Exemplo de utilização

O cenário de demonstração começa com Ana Souza e preserva o fluxo original do sistema. Em seguida, cadastra Bruno Lima, cria o plano de adaptação com o Builder e demonstra a remoção e a reinscrição do observador de meta semanal. Os identificadores foram abreviados no exemplo porque são gerados novamente a cada execução.

```text
[CLIENT] Plano 'Treino A' criado para Ana Souza.
[CLIENT] Realizando dois check-ins para demonstrar o Observer...
[AUDITORIA] Check-in 9bf4cd33-... registrado para o aluno 028c2b6a-... em 2026-11-12T02:13:03Z.
[ESTATISTICA] Total de check-ins do aluno 028c2b6a-...: 1.
[NOTIFICACAO] O aluno 028c2b6a-... realizou um check-in.
...
[CLIENT] Removendo o observador de notificação e fazendo um terceiro check-in...
[AUDITORIA] Check-in 2a90600a-... registrado para o aluno 028c2b6a-... em 2026-11-12T02:13:03Z.
[ESTATISTICA] Total de check-ins do aluno 028c2b6a-...: 3.
[CLIENT] Check-ins registrados: 3

[CLIENT] Parte 2: plano 'Adaptação', nível INICIANTE, duração de 4 semanas e meta de 1 dia(s) por semana.
[CLIENT] Aluno: Bruno Lima
[CLIENT] Objetivo: Iniciar a rotina de treinos
[CLIENT] Exercicios do plano:
         - Agachamento livre (PERNAS): 2 series de 10 repetições
         - Remada baixa (COSTAS): 2 series de 10 repetições
         
[CLIENT] Removendo o observador de meta semanal antes do primeiro check-in.
[CLIENT] Primeiro check-in: auditoria, estatística e notificação continuam inscritos.
[AUDITORIA] Check-in ec8818e0-... registrado para o aluno 2eeb710c-... em 2026-11-26T23:31:24Z.
[CLIENT] Reinscrevendo o observador de meta semanal.
[CLIENT] Segundo check-in no mesmo dia: a presença anterior já está salva.
[META SEMANAL] Aluno 2eeb710c-... atingiu a meta de 1 dia(s) na semana de 2026-11-23 com o plano 'Adaptação'.
[CLIENT] Terceiro check-in no mesmo dia: a mensagem da meta não deve se repetir.
[CLIENT] Check-ins de Bruno Lima: 3. Os três registros no mesmo dia representam um dia de treino.
```

## Padrões de projeto

### Builder

**Problema que resolve:** o plano de treino tem atributos obrigatórios (aluno, nome, objetivo) e opcionais (frequência, nível, duração, exercícios e observações). Sem o Builder, seriam necessários um construtor longo ou vários construtores. O Builder monta o plano passo a passo e valida tudo antes de criá-lo.

**Classes envolvidas:** `PlanoTreino` (produto imutável), `PlanoTreino.Builder`, `Exercicio`, `GrupoMuscular`, `NivelTreino` e `AcademiaClient` (uso).

```java
PlanoTreino plano = PlanoTreino.builder(aluno.id(), "Treino A", "Condicionamento físico")
		.nivel(NivelTreino.INICIANTE)
		.duracaoSemanas(4)
        .frequenciaSemanal(3)
        .adicionarExercicio(new Exercicio("Agachamento livre", GrupoMuscular.PERNAS, 3, 12))
        .build();
```

O `build()` rejeita planos inválidos, como frequência fora de 1 a 7, duração fora de 1 a 52 semanas ou uma lista de exercícios vazia.

### Observer

**Problema que resolve:** ao fazer check-in, vários comportamentos precisam reagir (auditoria, notificação, estatística e acompanhamento da meta semanal). Sem o Observer, o `CheckInService` teria de conhecer cada um deles. Com o Observer, ele apenas avisa quem estiver inscrito, e novos comportamentos não exigem alterar o serviço.

**Classes envolvidas:**
- `CheckInService`: *Subject*; possui `registrarObserver`, `removerObserver` e notifica após salvar o check-in.
- `CheckInObserver`: interface dos observadores.
- `AuditoriaFrequenciaObserver`, `NotificacaoObserver`, `EstatisticaFrequenciaObserver` e `MetaSemanalObserver`: observadores concretos.
- `CheckIn`: evento enviado aos observadores.

## Possíveis extensões futuras

- Plano de assinatura (preço, duração e benefícios), reaproveitando o Builder.
- Novos observers, como envio de e-mail, SMS ou alerta de ausência prolongada.
- Persistência em banco de dados, API REST e interface web.

## Contribuição individual

| Integrantes|

| Gustavo de Freitas Cardoso |

| Gustavo Luís da Silva |

| João Miguel Fortunato Rita |

## Parte 2 — Extensão da equipe Pet Shop

Equipe extensora formada por Guilherme, Murilo, Thiago e João Paulo. O desenvolvimento ocorre na branch `feature/extensao-parte2` do fork `guilhermegm-2k26/academia-backend`. O PR final terá como destino a branch `main` de `JoaoMiguelRita/academia-backend`.

### Extensão do Builder

O `PlanoTreino.Builder` recebeu os passos `nivel()` e `duracaoSemanas()`. O nível é representado por `NivelTreino`, com as opções `INICIANTE`, `INTERMEDIARIO` e `AVANCADO`. A duração aceita valores de 1 a 52 semanas. Chamadas já existentes ao Builder continuam válidas, com nível `INICIANTE` e duração de quatro semanas como valores padrão.

A demonstração acrescenta um plano **Adaptação**, com exercícios de duas séries, nível iniciante, duração de quatro semanas e frequência de um dia por semana. Esses valores são configurados no Builder. A duração também define o período acompanhado pelo novo observador.

### Extensão do Observer

`MetaSemanalObserver` implementa o contrato existente `CheckInObserver`. O Spring o registra junto dos demais observadores. O método `acompanharPlano()` seleciona um plano por aluno, e a frequência semanal desse plano passa a ser a meta de dias de treino.

Após cada check-in salvo, o observador consulta as presenças em memória e conta os dias diferentes da mesma semana, de segunda-feira a domingo, no fuso `America/Sao_Paulo`. Ao atingir a meta, imprime uma mensagem no console. A mensagem é emitida uma vez por plano e semana. Vários check-ins no mesmo dia representam um dia de treino. A contagem inclui a data de criação do plano e termina antes da data de criação acrescida da duração em semanas. Uma semana parcialmente coberta pela duração mantém a frequência configurada como meta.

Alunos com um plano selecionado por `acompanharPlano()` participam desse acompanhamento. Selecionar outro plano para o mesmo aluno substitui o plano acompanhado. As presenças já salvas no período são consideradas na próxima notificação. Planos, presenças e registros de metas notificadas ficam em memória durante a execução.

### Como executar e conferir a extensão

Na raiz do projeto, execute:

```bash
mvn clean verify
mvn spring-boot:run
```

A execução começa com o cenário original de Ana Souza e seus três check-ins. Em seguida, o console mostra o plano de adaptação de Bruno Lima. No primeiro check-in, o observador de meta está removido; auditoria, estatística e notificação continuam recebendo o evento. Ele é reinscrito antes do segundo check-in e emite a mensagem `[META SEMANAL]`, considerando as presenças já salvas. O terceiro registro no mesmo dia não repete a mensagem. Ao final, são exibidos três check-ins, equivalentes a um dia de treino.

`mvn test` executa os nove testes originais do projeto. Criar novos arquivos de testes é opcional. Para conferir a extensão na apresentação, executem o cenário de adaptação e expliquem os passos do Builder e as mensagens do Observer.

### Andamento e contribuições da equipe extensora

As etapas distribuídas entre os integrantes foram concluídas. A extensão do Builder, o observador de meta semanal, a apresentação completa do plano, a demonstração de inscrição e remoção do observador e a validação final estão reunidas nesta branch.

| Integrante | Entrega | Situação | Commit |
| --- | --- | --- | --- |
| Guilherme | Ampliar o Builder, criar o observador de meta semanal, integrar a demonstração inicial e organizar a documentação. | Registrada em quatro commits. | `c8b6ff4`, `3705bb0`, `3b11dab`, `e43b908` |
| Murilo | Completar a exibição do plano de adaptação, incluindo aluno, objetivo e exercícios, e conferir a configuração pelo Builder. | Concluída. | `9fa74a2`, `5278a03` |
| Thiago | Demonstrar a remoção e a reinscrição do observador de metas, com três check-ins no mesmo dia. | Concluída. | `f0efa5f` |
| João Paulo | Validar a execução final e fechar o README com exemplos e contribuições reais. | Concluída. | Commit de fechamento da documentação |

Cada integrante registrou sua entrega com a própria conta do GitHub. A responsabilidade de abrir o PR final deve ser combinada pela equipe, observando a orientação recebida sobre integrantes que ainda estejam sem commit autoral na Parte 1.
