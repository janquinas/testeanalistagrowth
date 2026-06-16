import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class analistagrowth {

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Erro: Informe o caminho do arquivo CSV.");
            System.out.println("Exemplo: java analistagrowth dataset_01_parceiroA.csv");
            return;
        }

        String arquivoCsv = args[0]; 
   
        String apiKey = System.getenv("GEMINI_API_KEY"); 

        try {
            System.out.println("1. Lendo o arquivo: " + arquivoCsv + "...");
            String dadosDoArquivo = Files.readString(Path.of(arquivoCsv));
            
            System.out.println("2. Estruturando o Prompt de Engenharia...");
           
            String prompt = "Você é um analista de Growth sênior do Méliuz.\n\n" +
                            "DADOS BRUTOS DO TESTE A/B:\n" + dadosDoArquivo + "\n\n" +
                            "INSTRUÇÕES OBRIGATÓRIAS DE EXECUÇÃO:\n" +
                            "1. Analise criticamente os dados acima (taxas de conversão, receita, custo de cashback vs comissão).\n" +
                            "2. Escreva um relatório gerencial claro e apresentável para um gestor.\n" +
                            "3. Responda explicitamente: Dado esse teste A/B, qual variante de cashback devemos escalar pra 100% do tráfego?\n" +
                            "4. A ÚLTIMA LINHA da sua resposta deve conter APENAS o marcador [LINHA_CSV] seguido do resumo estruturado exatamente neste formato:\n" +
                            "[LINHA_CSV] Nome do Teste; Variante Vencedora; Justificativa Curta de Negócio\n" +
                            "Atenção: Não adicione blocos de código ou aspas na última linha. Escreva o texto puro começando com [LINHA_CSV].";
            
            String textoLimpo = prompt.replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");
            String corpoRequisicao = "{ \"contents\": [{ \"parts\":[{\"text\": \"" + textoLimpo + "\"}] }] }";
            
            System.out.println("3. Enviando dados para o modelo gemini-3.5-flash...");
            String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=" + apiKey;
            
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(corpoRequisicao))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String jsonResposta = response.body();

            String textoRelatorio = extrairTextoDoJson(jsonResposta);

            System.out.println("4. Salvando o relatorio gerencial...");
            String relatorioFinal = "Relatorio_" + arquivoCsv.replace(".csv", ".txt");
            Files.writeString(Path.of(relatorioFinal), textoRelatorio);
            System.out.println("-> Sucesso: " + relatorioFinal + " gerado!");
          
            if (textoRelatorio.contains("[LINHA_CSV]")) {
                System.out.println("5. Extraindo dados para a planilha de controle...");
                String linhaCsv = textoRelatorio.split("\\[LINHA_CSV\\]")[1].trim();
             
                Files.writeString(Path.of("controle_testes.csv"), linhaCsv + "\n", 
                                  StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                System.out.println("-> Linha adicionada em controle_testes.csv!");
            } else {
                System.out.println("Aviso: O Gemini nao retornou o marcador [LINHA_CSV] corretamente.");
            }
            
            System.out.println("\nProcesso concluido com sucesso!");

        } catch (Exception e) {
            System.out.println("Erro critico durante a execucao:");
            e.printStackTrace();
        }
    }

    private static String extrairTextoDoJson(String json) {
        String chave = "\"text\": \"";
        int inicio = json.indexOf(chave);
        if (inicio == -1) {
            return "Erro ao processar JSON. Resposta bruta:\n" + json;
        }
        inicio += chave.length();
        
        int fim = inicio;
        while (fim < json.length()) {
            if (json.charAt(fim) == '"' && json.charAt(fim - 1) != '\\') {
                break;
            }
            fim++;
        }
        
        String texto = json.substring(inicio, fim);
        return texto.replace("\\n", "\n").replace("\\\"", "\"");
    }
}
