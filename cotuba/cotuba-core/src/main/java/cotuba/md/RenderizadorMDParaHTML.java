package cotuba.md;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.commonmark.node.AbstractVisitor;
import org.commonmark.node.Heading;
import org.commonmark.node.Node;
import org.commonmark.node.Text;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.stereotype.Component;

import cotuba.builder.CapituloBuilder;
import cotuba.domain.Capitulo;
import cotuba.plugin.AoRenderizarHTML;

@Component
public class RenderizadorMDParaHTML {

	public List<Capitulo> renderiza(Path diretorioDosMD) {
		return obtemArquivosMD(diretorioDosMD)
				.stream()
				.map(arquivoMD -> {
					CapituloBuilder capituloBuilder = new CapituloBuilder();
					Node document = parseDoMD(arquivoMD, capituloBuilder);
					renderizaParaHTML(arquivoMD, capituloBuilder, document);
					return capituloBuilder.constroi();
				})
				.collect(Collectors.toList());
	}

	private List<Path> obtemArquivosMD(Path diretorioDosMD) {
		PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:**/*.md");
		try (Stream<Path> arquivosMD = Files.list(diretorioDosMD)) {
			return arquivosMD
					.filter(matcher::matches)
					.sorted()
					.collect(Collectors.toList());
		} catch (IOException ex) {
			throw new IllegalStateException(
					"Erro tentando encontrar arquivos .md em " + diretorioDosMD.toAbsolutePath(), ex);
		}

	}

	private Node parseDoMD(Path arquivoMD, CapituloBuilder capituloBuilder) {
		Parser parser = Parser.builder().build();
		Node document = null;
		try {
			document = parser.parseReader(Files.newBufferedReader(arquivoMD));
			document.accept(new AbstractVisitor() {
				@Override
				public void visit(Heading heading) {
					if (heading.getLevel() == 1) {
						// capítulo
						String tituloDoCapitulo = ((Text) heading.getFirstChild()).getLiteral();
						// TODO: usar título do capítulo
						capituloBuilder.comTitulo(tituloDoCapitulo);
					} else if (heading.getLevel() == 2) {
						// seção
					} else if (heading.getLevel() == 3) {
						// título
					}
				}

			});
		} catch (Exception ex) {
			throw new IllegalStateException("Erro ao fazer parse do arquivo " + arquivoMD, ex);
		}
		return document;
	}

	private void renderizaParaHTML(Path arquivoMD, CapituloBuilder capituloBuilder, Node document) {
		try {
			HtmlRenderer renderer = HtmlRenderer.builder().build();
			String html = renderer.render(document);
			capituloBuilder.comConteudoHTML(html);
			String htmlModificado = AoRenderizarHTML.renderizou(html);

		} catch (Exception ex) {
			throw new IllegalStateException("Erro ao renderizar para HTML o arquivo " + arquivoMD, ex);
		}
	}
	
}
