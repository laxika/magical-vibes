package com.github.laxika.magicalvibes.model;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Shared normalization and matching for Legacy's keyword or ability-word choice. */
public final class LegacyWordSupport {

    private static final Pattern ABILITY_WORD_HEADING =
            Pattern.compile("^\\s*(.+?)\\s*—\\s*.+$");

    /** Cycling is not a {@link Keyword}; a card has it when it carries a cycling ability. */
    public static final String CYCLING = "CYCLING";

    private LegacyWordSupport() {
    }

    public static String normalize(String word) {
        return word == null ? "" : word.trim()
                .toUpperCase(Locale.ROOT)
                .replace('_', ' ')
                .replaceAll("\\s+", " ");
    }

    public static boolean cardHasWord(Card card, String chosenWord) {
        if (card == null) {
            return false;
        }
        String normalizedChoice = normalize(chosenWord);
        if (normalizedChoice.isEmpty()) {
            return false;
        }
        if (CYCLING.equals(normalizedChoice)
                && card.getHandActivatedAbilities().stream().anyMatch(ActivatedAbility::isCyclingAbility)) {
            return true;
        }
        if (card.getKeywords().stream()
                .anyMatch(keyword -> normalize(keyword.name()).equals(normalizedChoice))) {
            return true;
        }
        if (card.getCardText() == null) {
            return false;
        }
        for (String line : card.getCardText().split("\\R")) {
            Matcher matcher = ABILITY_WORD_HEADING.matcher(line);
            if (matcher.matches() && normalize(matcher.group(1)).equals(normalizedChoice)) {
                return true;
            }
        }
        return false;
    }
}
