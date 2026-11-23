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

O cenário de demonstração cadastra uma aluna, cria um plano com o Builder, faz dois check-ins (os três observadores reagem), remove um observador e faz um terceiro check-in.

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
```

## Padrões de projeto

### Builder

**Problema que resolve:** o plano de treino tem atributos obrigatórios (aluno, nome, objetivo) e opcionais (frequência, exercícios, observações). Sem o Builder, seriam necessários um construtor longo ou vários construtores. O Builder monta o plano passo a passo e valida tudo antes de criá-lo.

**Classes envolvidas:** `PlanoTreino` (produto imutável), `PlanoTreino.Builder`, `Exercicio`, `GrupoMuscular` e `AcademiaClient` (uso).

```java
PlanoTreino plano = PlanoTreino.builder(aluno.id(), "Treino A", "Condicionamento físico")
        .frequenciaSemanal(3)
        .adicionarExercicio(new Exercicio("Agachamento livre", GrupoMuscular.PERNAS, 3, 12))
        .build();
```

O `build()` rejeita planos inválidos (frequência fora de 1 a 7 ou sem exercícios).

### Observer

**Problema que resolve:** ao fazer check-in, vários comportamentos precisam reagir (auditoria, notificação, estatística). Sem o Observer, o `CheckInService` teria de conhecer cada um deles. Com o Observer, ele apenas avisa quem estiver inscrito, e novos comportamentos não exigem alterar o serviço.

**Classes envolvidas:**
- `CheckInService`: *Subject*; possui `registrarObserver`, `removerObserver` e notifica após salvar o check-in.
- `CheckInObserver`: interface dos observadores.
- `AuditoriaFrequenciaObserver`, `NotificacaoObserver`, `EstatisticaFrequenciaObserver`: observadores concretos.
- `CheckIn`: evento enviado aos observadores.

## Possíveis extensões futuras

- Plano de assinatura (preço, duração e benefícios), reaproveitando o Builder.
- Novos observers, como alerta de meta semanal ou envio de e-mail/SMS.
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

A execução começa com o cenário original de Ana Souza e seus três check-ins. Em seguida, o console mostra o plano de adaptação de Bruno Lima, dois check-ins e uma mensagem iniciada por `[META SEMANAL]`. O segundo registro ocorre no mesmo dia e mantém uma única mensagem de meta. Os observadores de auditoria, estatística e notificação também recebem os novos check-ins.

`mvn test` continua executando os nove testes originais. Os testes específicos do nível, da duração e da meta semanal estão distribuídos nas tarefas de continuidade.

### Andamento e contribuições da equipe extensora

A preparação atual corresponde a aproximadamente **65% do escopo planejado**, por distribuição de esforço. Essa estimativa considera o núcleo funcional dos dois padrões, a demonstração inicial e a documentação inicial. As entregas restantes somam os testes de regressão, a melhoria da demonstração e o fechamento da documentação.

| Integrante | Entrega planejada | Situação | Commit |
| --- | --- | --- | --- |
| Guilherme | Ampliar o Builder, criar o observador de meta semanal, integrar a demonstração inicial e organizar a documentação. | Preparada localmente para revisão e commits incrementais. | A preencher após os commits. |
| Murilo | Automatizar a validação dos novos campos e a compatibilidade do Builder. | Pendente. | A preencher após o commit. |
| Thiago | Automatizar os cenários de meta semanal, duplicidade e integração do Observer. | Pendente. | A preencher após o commit. |
| João Paulo | Completar o resumo da demonstração, validar a execução e fechar o README com as contribuições reais. | Pendente. | A preencher após o commit. |

Cada integrante registra sua entrega com a própria conta do GitHub. A responsabilidade de abrir o PR final deve ser combinada pela equipe, observando a orientação recebida sobre integrantes que ainda estejam sem commit autoral na Parte 1. Ao concluir as tarefas, atualizem a situação e os hashes na tabela.
