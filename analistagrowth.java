import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class analistagrowth {

    public static void main(String[] args) {
 
        String minhaChaveReal = "SUA_CHAVE_DE_API_AQUI"; 

        if (args.length == 0) {
            System.out.println("Erro: Informe o caminho do arquivo CSV.");
            return;
        }

        String arquivoCsv = args[0]; 

        try {
            System.out.println("1. Lendo o arquivo: " + arquivoCsv + "...");
            String dadosDoArquivo = Files.readString(Path.of(arquivoCsv));
            
            System.out.println("2. Estruturando o Prompt...");
            String prompt = "Você é um analista de Growth sênior do Méliuz.\n\n" +
                            "DADOS BRUTOS DO TESTE A/B:\n" + dadosDoArquivo + "\n\n" +
                            "INSTRUÇÕES OBRIGATÓRIAS:\n" +
                            "1. Analise os dados acima.\n" +
                            "2. Escreva um relatório gerencial indicando qual variante escalar para 100%.\n" +
                            "3. A ÚLTIMA LINHA deve conter APENAS o marcador [LINHA_CSV] seguido de: Nome do Teste; Variante Vencedora; Justificativa.";
            
            String textoLimpo = prompt.replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");
            String corpoRequisicao = "{ \"contents\": [{ \"parts\":[{\"text\": \"" + textoLimpo + "\"}] }] }";
            
            System.out.println("3. Enviando dados para o Google Gemini...");

            String url = "https://generativelanguage.googleapis.com/v1/models/gemini-2.5-flash:generateContent?key=" + minhaChaveReal;
            
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
                String[] partes = textoRelatorio.split("\\[LINHA_CSV\\]");
                if (partes.length > 1) {
                    String linhaCsv = partes[1].trim();
                    Files.writeString(Path.of("controle_testes.csv"), linhaCsv + "\n", 
                                      StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                    System.out.println("-> Linha adicionada em controle_testes.csv!");
                }
            }
            
            System.out.println("\nProcesso concluido com sucesso!");

        } catch (Exception e) {
            System.out.println("Erro critico:");
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
