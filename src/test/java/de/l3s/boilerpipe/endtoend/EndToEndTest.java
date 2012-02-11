package de.l3s.boilerpipe.endtoend;

import de.l3s.boilerpipe.BoilerpipeProcessingException;
import de.l3s.boilerpipe.document.TextDocument;
import de.l3s.boilerpipe.filters.english.IgnoreBlocksAfterContentFilter;
import de.l3s.boilerpipe.filters.english.NumWordsRulesClassifier;
import de.l3s.boilerpipe.filters.english.TerminatingBlocksFinder;
import de.l3s.boilerpipe.filters.heuristics.BlockProximityFusion;
import de.l3s.boilerpipe.filters.heuristics.DocumentTitleMatchClassifier;
import de.l3s.boilerpipe.filters.heuristics.ExpandTitleToContentFilter;
import de.l3s.boilerpipe.filters.heuristics.KeepLargestBlockFilter;
import de.l3s.boilerpipe.filters.simple.BoilerplateBlockFilter;
import de.l3s.boilerpipe.sax.BoilerpipeSAXInput;
import de.l3s.boilerpipe.sax.HTMLFetcher;
import org.junit.Test;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.URL;

public class EndToEndTest {
    @Test
    public void shouldGoEndToEnd() throws IOException, SAXException, BoilerpipeProcessingException {
        URL url = new URL("http://privacysos.org/node/475");
        TextDocument doc = new BoilerpipeSAXInput(HTMLFetcher.fetch(url).toInputSource()).getTextDocument();

        boolean blocksProcessed = TerminatingBlocksFinder.INSTANCE.process(doc)
                | new DocumentTitleMatchClassifier(doc.getTitle()).process(doc)
                | NumWordsRulesClassifier.INSTANCE.process(doc)
                | IgnoreBlocksAfterContentFilter.DEFAULT_INSTANCE.process(doc)
                | BlockProximityFusion.MAX_DISTANCE_1.process(doc)
                | BoilerplateBlockFilter.INSTANCE.process(doc)
                | BlockProximityFusion.MAX_DISTANCE_1_CONTENT_ONLY.process(doc)
                | KeepLargestBlockFilter.INSTANCE.process(doc)
                | ExpandTitleToContentFilter.INSTANCE.process(doc);

        System.out.println(doc.getContent());
    }

}
