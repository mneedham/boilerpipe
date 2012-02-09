package de.l3s.boilerpipe.filters.english;

import de.l3s.boilerpipe.BoilerpipeProcessingException;
import de.l3s.boilerpipe.document.TextBlock;
import de.l3s.boilerpipe.document.TextDocument;
import org.junit.Test;

import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class NumWordsRulesClassifierTest {
    @Test
    public void blockIsContentIfItHasALotOfWords() throws BoilerpipeProcessingException {
        String words = "my paragraph with lots and lots of words so it thinks it's a real block of text. Because it is isn't it.";
        List<TextBlock> textBlocks = asList(new TextBlock(words, null, words.length(), 0, 0, 0, 0));

        TextDocument document = new TextDocument(textBlocks);

        NumWordsRulesClassifier.INSTANCE.process(document);

        assertThat(document.getTextBlocks().get(0).isContent(), is(true));
    }
}
