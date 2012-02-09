/**
 * boilerpipe
 *
 * Copyright (c) 2009 Christian Kohlschütter
 *
 * The author licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.l3s.boilerpipe.filters.english;

import de.l3s.boilerpipe.BoilerpipeFilter;
import de.l3s.boilerpipe.BoilerpipeProcessingException;
import de.l3s.boilerpipe.document.TextBlock;
import de.l3s.boilerpipe.document.TextDocument;

import java.util.List;
import java.util.ListIterator;

/**
 * Classifies {@link TextBlock}s as content/not-content through rules that have
 * been determined using the C4.8 machine learning algorithm, as described in
 * the paper "Boilerplate Detection using Shallow Text Features" (WSDM 2010),
 * particularly using number of words per block and link density per block.
 *
 * @author Christian Kohlschütter
 */
public class NumWordsRulesClassifier implements BoilerpipeFilter {
    public static final NumWordsRulesClassifier INSTANCE = new NumWordsRulesClassifier();

    /**
     * Returns the singleton instance for RulebasedBoilerpipeClassifier.
     */
    public static NumWordsRulesClassifier getInstance() {
        return INSTANCE;
    }

    public boolean process(TextDocument doc) throws BoilerpipeProcessingException {
        List<TextBlock> textBlocks = doc.getTextBlocks();

        ListIterator<TextBlock> it = textBlocks.listIterator();
        if (!it.hasNext()) {
            return false;
        }

        TextBlock currentBlock = it.next();
        TextBlock nextBlock = it.hasNext() ? it.next() : TextBlock.EMPTY_START;

        boolean hasChanges = currentBlock.setIsContent(currentBlockHasContent(TextBlock.EMPTY_START, currentBlock, nextBlock));

        if (nextBlock != TextBlock.EMPTY_START) {
            hasChanges = processBlocks(hasChanges, it, currentBlock, nextBlock);
        }

        return hasChanges;
    }

    private boolean processBlocks(boolean hasChanges, ListIterator<TextBlock> it, TextBlock currentBlock, TextBlock nextBlock) {
        if (!it.hasNext()) {
            return nextBlock.setIsContent(currentBlockHasContent(currentBlock, nextBlock, TextBlock.EMPTY_START)) | hasChanges;
        }

        TextBlock newNextBlock = it.next();
        return processBlocks(nextBlock.setIsContent(currentBlockHasContent(currentBlock, nextBlock, newNextBlock)) | hasChanges, it, nextBlock, newNextBlock);
    }


    private boolean currentBlockHasContent(final TextBlock prev, final TextBlock curr, final TextBlock next) {
        if (fewLinksInCurrentBlock(curr)) {
            if (fewLinksInPreviousBlock(prev)) {
                return curr.getNumWords() > 16 || next.getNumWords() > 15 || prev.getNumWords() > 4;
            } else {
                return curr.getNumWords() > 40 || next.getNumWords() > 17;
            }
        }
        return false;
    }

    private boolean fewLinksInCurrentBlock(TextBlock curr) {
        return curr.getLinkDensity() <= 0.333333;
    }

    private boolean fewLinksInPreviousBlock(TextBlock prev) {
        return prev.getLinkDensity() <= 0.555556;
    }
}