# car_system — Gerenciador de carros

API REST em **Spring Boot** para cadastro de carros, com PostgreSQL via Docker.

> **Status:** esqueleto inicial. A entidade `Carro` e o repositório existem, mas o controller ainda não tem endpoints. Veja o backlog abaixo.

## Como rodar

```bash
docker compose up -d   # sobe o PostgreSQL na porta 5432
./mvnw spring-boot:run
```

## Backlog
1. Configurar o datasource em `application.properties` usando variáveis de ambiente
2. CRUD completo em `CarroController` (`GET/POST/PUT/DELETE /carros`) com validação
3. Testes com `@WebMvcTest` e `@DataJpaTest`
4. CI no GitHub Actions (`./mvnw verify`)
