package de.l3s.boilerpipe.filters.english;

import de.l3s.boilerpipe.BoilerpipeProcessingException;
import de.l3s.boilerpipe.document.TextBlock;
import de.l3s.boilerpipe.document.TextDocument;
import de.l3s.boilerpipe.util.UnicodeTokenizer;
import org.junit.Test;

import java.util.List;
import java.util.regex.Pattern;

import static java.util.Arrays.asList;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class NumWordsRulesClassifierTest {
    @Test
    public void blockIsContentIfItHasALotOfWords() throws BoilerpipeProcessingException {
        String words = "my paragraph with lots and lots of words so it thinks it's a real block of text. Because it is isn't it.";
        List<TextBlock> textBlocks = asList(new TextBlock(words, null, wordCount(words), 0, 0, 0, 0));

        TextDocument document = new TextDocument(textBlocks);

        NumWordsRulesClassifier.INSTANCE.process(document);

        assertThat(document.getTextBlocks().get(0).isContent(), is(true));
    }

    @Test
    public void blockIsContentIfItDoesNotHaveVeryManyWordsButPreviousBlockHasHighWords() throws BoilerpipeProcessingException {
        String lotsOfWords = "my paragraph with lots and lots of words so it thinks it's a real block of text. Because it is isn't it.";
        String words = "too few words to be content";
        List<TextBlock> textBlocks = asList(
                new TextBlock(lotsOfWords, null, wordCount(lotsOfWords), 0, 0, 0, 0),
                new TextBlock(words, null, wordCount(words), 0, 0, 0, 0)
        );

        TextDocument document = new TextDocument(textBlocks);

        NumWordsRulesClassifier.INSTANCE.process(document);

        assertThat(document.getTextBlocks().get(1).isContent(), is(true));
    }

    @Test
    public void blockIsContentIfItDoesNotHaveVeryManyWordsButNextBlockHasHighWords() throws BoilerpipeProcessingException {
        String lotsOfWords = "my paragraph with lots and lots of words so it thinks it's a real block of text. Because it is isn't it.";
        String words = "too few words to be content";
        List<TextBlock> textBlocks = asList(
                new TextBlock(words, null, wordCount(words), 0, 0, 0, 0),
                new TextBlock(lotsOfWords, null, wordCount(lotsOfWords), 0, 0, 0, 0)

        );

        TextDocument document = new TextDocument(textBlocks);

        NumWordsRulesClassifier.INSTANCE.process(document);

        assertThat(document.getTextBlocks().get(0).isContent(), is(true));
    }

    @Test
    public void blockIsContentIfHasLotsOfWordsEvenIfPreviousBlockIsFullOfLinks() throws BoilerpipeProcessingException {
        String lotsOfWords = "my paragraph with lots and lots of words so it thinks it's a real block of text. Because it is isn't it.";
        String evenMoreWords = "even more words but they will all be links so it thinks it's a real block of text. Because it is isn't it." +
                "even more words but they will all be links so it thinks it's a real block of text. Because it is isn't it.";
        List<TextBlock> textBlocks = asList(
                new TextBlock(evenMoreWords, null, wordCount(lotsOfWords), wordCount(lotsOfWords), 0, 0, 0),
                new TextBlock(lotsOfWords, null, wordCount(evenMoreWords), 0, 0, 0, 0)

        );

        TextDocument document = new TextDocument(textBlocks);

        NumWordsRulesClassifier.INSTANCE.process(document);

        assertThat(document.getTextBlocks().get(1).isContent(), is(true));
    }

    @Test
    public void blockIsContentIfHasLowLinkDensityAndNextBlockHasLotsOfWordsEvenIfPreviousBlockIsFullOfLinks() throws BoilerpipeProcessingException {
        String lotsOfWords = "my paragraph with lots and lots of words so it thinks it's a real block of text. Because it is isn't it.";
        List<TextBlock> textBlocks = asList(
                new TextBlock(lotsOfWords, null, wordCount(lotsOfWords), wordCount(lotsOfWords), 0, 0, 0),
                new TextBlock(lotsOfWords, null, wordCount(lotsOfWords), 0, 0, 0, 0),
                new TextBlock(lotsOfWords, null, wordCount(lotsOfWords), 0, 0, 0, 0)

        );

        TextDocument document = new TextDocument(textBlocks);

        NumWordsRulesClassifier.INSTANCE.process(document);

        assertThat(document.getTextBlocks().get(1).isContent(), is(true));
    }

    @Test
    public void blockIsNotContentIfItHasLowLinkDensityButPreviousBlockIsFullOfLinksAndNextBlockDoesNotHaveLotsOfWords() throws BoilerpipeProcessingException {
        String lotsOfWords = "my paragraph with lots and lots of words so it thinks it's a real block of text. Because it is isn't it.";
        String fewWords = "barely any words";
        List<TextBlock> textBlocks = asList(
                new TextBlock(lotsOfWords, null, wordCount(lotsOfWords), wordCount(lotsOfWords), 0, 0, 0),
                new TextBlock(lotsOfWords, null, wordCount(lotsOfWords), 0, 0, 0, 0),
                new TextBlock(fewWords, null, wordCount(fewWords), 0, 0, 0, 0)

        );

        TextDocument document = new TextDocument(textBlocks);

        NumWordsRulesClassifier.INSTANCE.process(document);

        assertThat(document.getTextBlocks().get(1).isContent(), is(false));
    }

    @Test
    public void blockIsNotContentIfItHasLoadsOfLinks() throws BoilerpipeProcessingException {
        String words = "my paragraph with lots and lots of words so it thinks it's a real block of text. Because it is isn't it.";
        List<TextBlock> textBlocks = asList(
                new TextBlock(words, null, wordCount(words), wordCount(words) - 2, 0, 0, 0)
        );

        TextDocument document = new TextDocument(textBlocks);

        NumWordsRulesClassifier.INSTANCE.process(document);

        assertThat(document.getTextBlocks().get(0).isContent(), is(false));
    }

    @Test
    public void blockIsNotContentIfItDoesNotHaveVeryManyWordsAndPreviousBlockHasLowWords() throws BoilerpipeProcessingException {
        String words = "too few words to be content";
        List<TextBlock> textBlocks = asList(new TextBlock(words, null, wordCount(words), 0, 0, 0, 0));

        TextDocument document = new TextDocument(textBlocks);

        NumWordsRulesClassifier.INSTANCE.process(document);

        assertThat(document.getTextBlocks().get(0).isContent(), is(false));
    }

    private int wordCount(String value) {
        StringBuilder tokenBuffer = new StringBuilder(value);
        final String[] tokens = UnicodeTokenizer.tokenize(tokenBuffer);

        int wordCount = 0;
        for (String word : tokens) {
            if (isWord(word)) {
                wordCount++;
            }
        }
        return wordCount;
    }

    private static final Pattern PAT_VALID_WORD_CHARACTER = Pattern.compile("[\\p{L}\\p{Nd}\\p{Nl}\\p{No}]");


    private static boolean isWord(final String token) {
        return PAT_VALID_WORD_CHARACTER.matcher(token).find();
    }

}
