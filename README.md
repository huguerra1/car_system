# car_system — Gerenciador de carros

[![CI](https://github.com/huguerra1/car_system/actions/workflows/ci.yml/badge.svg)](https://github.com/huguerra1/car_system/actions/workflows/ci.yml)

API REST para cadastrar, consultar, atualizar e excluir carros, construída com Spring Boot 4.0, Java 21, Maven Wrapper, Lombok, JPA e PostgreSQL. Cada carro tem um `id` gerado e os campos `marca`, `modelo` e `ano`.

## Como rodar com Docker

É necessário ter Docker com Docker Compose instalado. Na raiz do projeto:

```bash
docker compose up --build -d
```

O comando constrói a imagem multi-stage com Eclipse Temurin 21 e inicia o PostgreSQL na porta 5432 e a API em `http://localhost:8080`. A API aguarda o banco ficar saudável. O primeiro build precisa de acesso à internet para baixar as imagens e as dependências Maven.

```bash
docker compose logs -f api
docker compose down
```

Os dados ficam no volume `postgres-data` e são preservados ao parar os serviços.

## Como rodar localmente

É necessário ter JDK 21. O Maven é fornecido pelo wrapper. Suba apenas o PostgreSQL e execute a aplicação:

```bash
docker compose up -d db
./mvnw spring-boot:run
```

No PowerShell, use `./mvnw.cmd` no lugar de `./mvnw`. Se o serviço `api` já estiver rodando no Docker, pare-o com `docker compose stop api` para liberar a porta 8080 antes de iniciar localmente.

O datasource usa estas variáveis de ambiente e valores padrão:

| Variável | Padrão |
| --- | --- |
| `DB_URL` | `jdbc:postgresql://localhost:5432/carrosdb` |
| `DB_USER` | `postgres` |
| `DB_PASSWORD` | `123456` |

O Hibernate atualiza o esquema ao iniciar (`spring.jpa.hibernate.ddl-auto=update`). No Compose, a API usa `db` como hostname do PostgreSQL.

## Endpoints

| Método | Caminho | Resposta de sucesso |
| --- | --- | --- |
| GET | `/carros` | 200, lista em ordem crescente de `id` |
| GET | `/carros/{id}` | 200, carro encontrado |
| POST | `/carros` | 201, carro criado e cabeçalho `Location` |
| PUT | `/carros/{id}` | 200, carro atualizado |
| DELETE | `/carros/{id}` | 204, sem corpo |

`marca` e `modelo` são obrigatórios, não podem ser vazios ou conter apenas espaços e aceitam até 60 caracteres. `ano` é obrigatório, de 1886 até o ano atual + 1 (inclusive). POST e PUT recebem os três campos; o `id` é gerado pela API e a atualização usa o identificador da URL. GET por id, PUT e DELETE retornam 404 quando o carro não existe.

Exemplos em Bash (no PowerShell, use `curl.exe` e ajuste as quebras de linha):

```bash
# Criar (substitua o id nos próximos exemplos pelo retornado em Location)
curl -i -X POST http://localhost:8080/carros \
  -H 'Content-Type: application/json' \
  -d '{"marca":"Toyota","modelo":"Corolla","ano":2020}'

# Listar
curl -i http://localhost:8080/carros

# Buscar por id
curl -i http://localhost:8080/carros/1

# Atualizar
curl -i -X PUT http://localhost:8080/carros/1 \
  -H 'Content-Type: application/json' \
  -d '{"marca":"Honda","modelo":"Civic","ano":2024}'

# Excluir
curl -i -X DELETE http://localhost:8080/carros/1
```

Erros de validação retornam 400 com mensagens por campo. Por exemplo, ao enviar `{"marca":" ","modelo":"Corolla","ano":2020}`:

```json
{
  "status": 400,
  "mensagem": "Dados inválidos",
  "erros": {
    "marca": ["marca é obrigatória"]
  }
}
```

## Como testar

```bash
./mvnw -o -B test
```

O modo offline (`-o`) exige as dependências e a distribuição Maven no cache local. Para baixá-las em um ambiente com internet, execute `./mvnw -B test`.

Os testes usam H2 em memória em modo PostgreSQL, configurado em `src/test/resources/application.properties`, sem precisar do Docker ou de um PostgreSQL em execução. A suíte cobre o carregamento do contexto, o controller com `@WebMvcTest` (sucesso, 404 e validação), o repositório com `@DataJpaTest` e o CRUD completo com `@SpringBootTest` e MockMvc.

## Backlog

- CI no GitHub Actions (`./mvnw verify`).
