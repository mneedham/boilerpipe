package de.l3s.boilerpipe.filters.english;

import de.l3s.boilerpipe.util.UnicodeTokenizer;

import java.util.regex.Pattern;

public class WordRelatedFunctions {
    public static int wordCount(String value) {
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


    public static boolean isWord(final String token) {
        return PAT_VALID_WORD_CHARACTER.matcher(token).find();
    }
}
