package com.github.laxika.magicalvibes.carddata;

import tools.jackson.databind.JsonNode;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Turns an upstream provider's raw rules text into the text this game prints on a card.
 *
 * <p>Provider-neutral: both loaders and the card browser run their text through here, so a card
 * reads identically whether it was loaded from Scryfall or MTGJSON.
 */
public final class OracleTextNormalizer {

    private OracleTextNormalizer() {
    }

    /** Strips reminder text in parentheses; returns null when nothing remains. */
    public static String cleanCardText(String rawText) {
        String cleaned = rawText
                .replaceAll(" *\\([^)]*\\)", "")
                .replaceAll(" +\n", "\n")
                .strip();
        return cleaned.isEmpty() ? null : cleaned;
    }

    /**
     * Upstream providers print a keyword list with only the first keyword capitalized ("Trample,
     * reach"); we capitalize every keyword in the list instead ("Trample, Reach").
     *
     * <p>Only a line whose every comma-separated segment is one of the card's own keywords is
     * rewritten, so rules text that merely mentions a keyword ("Enchanted creature gains trample")
     * is left alone. A segment may carry a parameter ("Ward {2}", "Protection from red"); matching
     * looks at the leading keyword only.
     *
     * @param keywordSource the node carrying this card's {@code keywords} array
     */
    public static String capitalizeKeywordLines(String cardText, JsonNode keywordSource) {
        if (cardText == null || !keywordSource.has("keywords")) {
            return cardText;
        }

        Set<String> keywords = new HashSet<>();
        for (JsonNode keyword : keywordSource.get("keywords")) {
            keywords.add(keyword.asText().toLowerCase(Locale.ROOT));
        }
        if (keywords.isEmpty()) {
            return cardText;
        }

        String[] lines = cardText.split("\n", -1);
        for (int i = 0; i < lines.length; i++) {
            String[] segments = lines[i].split(", ", -1);
            if (allKeywords(segments, keywords)) {
                lines[i] = capitalizeEach(segments);
            }
        }
        return String.join("\n", lines);
    }

    private static boolean allKeywords(String[] segments, Set<String> keywords) {
        for (String segment : segments) {
            String lower = segment.toLowerCase(Locale.ROOT);
            boolean matched = false;
            for (String keyword : keywords) {
                // equals first, so the charAt below only runs when the segment is strictly longer
                if (lower.equals(keyword)
                        || (lower.startsWith(keyword) && lower.charAt(keyword.length()) == ' ')) {
                    matched = true;
                    break;
                }
            }
            if (!matched) {
                return false;
            }
        }
        return true;
    }

    private static String capitalizeEach(String[] segments) {
        StringBuilder line = new StringBuilder();
        for (String segment : segments) {
            if (!line.isEmpty()) {
                line.append(", ");
            }
            line.append(Character.toUpperCase(segment.charAt(0))).append(segment, 1, segment.length());
        }
        return line.toString();
    }
}
