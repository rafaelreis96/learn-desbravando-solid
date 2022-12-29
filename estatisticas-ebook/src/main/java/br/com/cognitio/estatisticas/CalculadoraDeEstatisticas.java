package br.com.cognitio.estatisticas;

import java.text.Normalizer;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import cotuba.domain.Capitulo;
import cotuba.domain.Ebook;
import cotuba.plugin.Plugin;

public class CalculadoraDeEstatisticas implements Plugin {

	public String aposRenderizacao(String html) {
		return html;
	}

	public void aposGeracao(Ebook ebook) {
		
		var contagemDepalavras = new ContagemDePalavras();

		
		for(Capitulo capitulo : ebook.getCapitulos()) {
			String html = capitulo.getConteudoHTML();
			
			Document doc = Jsoup.parse(html);
			
			String textoDoCapitulo = doc.body().text();
			
			String textoDoCapituloSemPontuacao = textoDoCapitulo.replaceAll("\\p{Punct}}", "");
			
			String decomposta = Normalizer.normalize(textoDoCapituloSemPontuacao, Normalizer.Form.NFD);
			
			String textoSemAcentos = decomposta.replaceAll("[^\\p{ASCII}]", "");
			
			String[] palavras = textoSemAcentos.split("\\s+");

			
			
			for(String palavra: palavras) {
				
				String emMaiusculas = palavra.toUpperCase();

				contagemDepalavras.adicionaPalavra(emMaiusculas);
			}
			
		}
		
		for(Map.Entry<String, Integer> contagem: contagemDepalavras.entrySet()) {
			
			String palavra = contagem.getKey();
			
			Integer ocorrencias = contagem.getValue();
			
			System.out.println(palavra + ": " + ocorrencias);
		}
		
	}

}