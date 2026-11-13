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
