package de.l3s.boilerpipe.filters.heuristics;

import de.l3s.boilerpipe.BoilerpipeProcessingException;
import de.l3s.boilerpipe.document.TextBlock;
import de.l3s.boilerpipe.document.TextDocument;
import org.junit.Test;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;

import static de.l3s.boilerpipe.filters.english.WordRelatedFunctions.wordCount;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class BlockProximityFusionTest {
    @Test
    public void shouldDoNothingIfWeOnlyGiveItOneTextBlock() throws BoilerpipeProcessingException {
        TextDocument document = new TextDocument(asList(contentBlock("word")));

        assertThat(process(document), is(false));
    }

    @Test
    public void shouldDoNothingIfWeGiveItAllNoContentTextBlocksAndWeWantContentBlocksOnly() throws BoilerpipeProcessingException {
        TextDocument document = new TextDocument(asList(nonContentTextBlock(), nonContentTextBlock(), nonContentTextBlock()));

        assertThat(process(document), is(false));
    }

    @Test
    public void shouldMergeTwoContentBlocksTogether() throws BoilerpipeProcessingException, IOException, SAXException {
        // I think it increments offset variable up until the first bit of content it finds...

        ArrayList<TextBlock> textBlocks = asArrayList(nonContentTextBlock(), contentBlock("some words"), contentBlock("some words"));

        TextDocument document = new TextDocument(textBlocks);
        process(document);

        assertThat(document.getContent(), equalTo("some words\nsome words"));
    }

    @Test
    public void willBlowUpIfYouUseAListConstructedWithasList() throws BoilerpipeProcessingException {
        TextDocument document = new TextDocument(asList(nonContentTextBlock(), contentBlock("some words"), contentBlock("followed by more words")));

        try {
            process(document);
            fail();
        } catch (java.lang.UnsupportedOperationException e) {

        }
    }

    private ArrayList<TextBlock> asArrayList(TextBlock... textBlocks) {
        ArrayList<TextBlock> textBlocksList = new ArrayList<TextBlock>();
        Collections.addAll(textBlocksList, textBlocks);
        return textBlocksList;
    }

    private boolean process(TextDocument document) throws BoilerpipeProcessingException {
        return BlockProximityFusion.MAX_DISTANCE_1.process(document);
    }

    private TextBlock contentBlock(String words) {
        TextBlock textBlock = new TextBlock(words, new BitSet(), wordCount(words), 0, 0, 0, 0);
        textBlock.setIsContent(true);
        return textBlock;
    }

    private TextBlock nonContentTextBlock() {
        TextBlock textBlock = new TextBlock("word", new BitSet(), 1, 0, 0, 0, 0);
        textBlock.setIsContent(false);
        return textBlock;
    }
}
