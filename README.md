# AnalistaGrowth
Analisador Automatizado de Testes A/B - Growth AI-Native

Descrição
- Programa Java que lê um arquivo CSV com resultados de um teste A/B, envia os dados para a API de linguagem generativa do Google (Gemini) com instruções de análise e produz um relatório de texto.
- Se a resposta gerada contiver a tag final `[LINHA_CSV]`, o programa extrai a parte após essa tag e a acrescenta em `controle_testes.csv` (formato: "Nome do Teste; Variante Vencedora; Justificativa Curta de Negócio").

Requisitos
- Java 11 ou superior (usa `java.net.http`).
- Variável de ambiente `GOOGLE_API_KEY` definida com sua chave válida da Google Generative Language API.

Como configurar a chave (Windows PowerShell)
```powershell
setx GOOGLE_API_KEY "SUA_CHAVE_AQUI"
```

Compilar e executar

- Observação importante: o nome da classe pública deve coincidir com o nome da classe dentro do arquivo Java. No Windows o sistema de arquivos é normalmente case‑insensitive, mas o nome da classe (ex.: `AnalistaGrowth`) é sensível ao executar o comando `java`.

1. Compile (pelo nome do arquivo):
```powershell
javac AnalistaGrowth.java

```
2. Execute passando o nome da classe (não o nome do arquivo) e o caminho do CSV como argumento:
```powershell
java analistagrowth caminho\para\seu_arquivo.csv
```
Exemplo:
```powershell
java analistagrowth dataset_01_parceiroA.csv
```

O que o programa gera
- `Relatorio_<nome_do_arquivo>.txt`: arquivo de texto com o relatório completo gerado pela API.
- `controle_testes.csv`: arquivo de resumo onde cada execução que contiver `[LINHA_CSV]` adiciona uma linha com o resumo estruturado.

Observações e dicas
- A variável `GOOGLE_API_KEY` deve conter apenas a chave; não inclua outros caracteres.
- A classe pública usada pelo programa é `AnalistaGrowth`. Se você tiver um arquivo com outro nome (`analistagrowth.java`), compile pelo nome do arquivo mas execute pelo nome da classe:
```powershell
javac analistagrowth.java
java AnalistaGrowth dataset_01_parceiroA.csv
```
- Recomendo manter o arquivo e a classe com o mesmo nome (`AnalistaGrowth.java` / `AnalistaGrowth`) para evitar confusão.
- Em caso de falha HTTP ou resposta inesperada, o programa imprime a resposta bruta para diagnóstico.

##  Como Visualizar a Planilha de Controle Consolidadada

O programa gera de forma totalmente automatizada o arquivo centralizador chamado `controle_testes.csv` acumulando o resumo estruturado de todos os parceiros à medida que são processados. 

Para visualizar os dados formatados em colunas e linhas perfeitas, você pode abrir o arquivo utilizando qualquer um dos dois métodos abaixo:

### Opção 1: Pelo Google Planilhas 
1. Acesse o seu [Google Drive](https://drive.google.com/).
2. Faça o upload do arquivo `controle_testes.csv` gerado na pasta do seu projeto.
3. Clique duas vezes sobre o arquivo enviado e escolha a opção **"Abrir com o Google Planilhas"** no topo da tela.
4. O Google interpretará os separadores de ponto e vírgula (`;`) automaticamente e separará os dados em colunas perfeitas (`Nome do Teste`, `Variante Vencedora`, `Justificativa`).

### Opção 2: Pelo Microsoft Excel
Caso abra diretamente no Microsoft Excel e as informações apareçam grudadas em uma única linha, siga este ajuste rápido de importação:
1. Abra um arquivo em branco no Excel.
2. Vá até a aba **Dados** no menu superior e clique em **De Texto/CSV**.
3. Selecione o arquivo `controle_testes.csv` na pasta do seu projeto e clique em **Importar**.
4. Na janela de visualização que abrir, certifique-se de que o campo **Delimitador** esteja marcado como **Ponto e vírgula**.
5. Clique em **Carregar** e a sua tabela de controle estará totalmente estruturada.
