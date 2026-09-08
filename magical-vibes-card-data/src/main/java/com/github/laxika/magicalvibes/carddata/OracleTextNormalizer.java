package com.github.laxika.magicalvibes.carddata;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
     * reach" or "Flying; fear"); we capitalize every keyword in the list instead.
     *
     * <p>Only a line whose every comma- or semicolon-separated segment is one of the card's own keywords is
     * rewritten, so rules text that merely mentions a keyword ("Enchanted creature gains trample")
     * is left alone. A segment may carry a parameter ("Ward {2}", "Protection from red"); matching
     * looks at the leading keyword only.
     *
     * @param cardKeywords this card's keywords in their raw upstream spelling. Not mapped
     *                     {@code Keyword} values: matching has to recognise spellings the enum does
     *                     not cover ("Ward {2}", "Protection from red"), which would otherwise stop
     *                     being capitalised.
     */
    public static String capitalizeKeywordLines(String cardText, Collection<String> cardKeywords) {
        if (cardText == null || cardKeywords.isEmpty()) {
            return cardText;
        }

        Set<String> keywords = new HashSet<>();
        for (String keyword : cardKeywords) {
            keywords.add(keyword.toLowerCase(Locale.ROOT));
        }

        String[] lines = cardText.split("\n", -1);
        for (int i = 0; i < lines.length; i++) {
            String[] segments = keywordSegments(lines[i]);
            if (allKeywords(segments, keywords)) {
                lines[i] = capitalizeEach(lines[i]);
            }
        }
        return String.join("\n", lines);
    }

    /**
     * The subset of {@code candidates} that this text states as the card's own keywords.
     *
     * <p>Scryfall's {@code keywords} array on a double-faced card is the union of both faces, so a
     * back face's own keywords have to be narrowed back out of that combined list. A candidate
     * counts only when it heads a segment of a line whose every comma- or semicolon-separated segment is itself a
     * keyword — the same rule {@link #capitalizeKeywordLines} uses — so a keyword the card merely
     * mentions ("Enchanted creature gains trample") or grants to others ("Creatures you control
     * have flying") is not counted as the card's own.
     *
     * <p>Returned values keep the spelling they had in {@code candidates}.
     */
    public static Set<String> keywordsStatedIn(String cardText, Collection<String> candidates) {
        if (cardText == null || candidates.isEmpty()) {
            return Set.of();
        }

        Map<String, String> bySpelling = new HashMap<>();
        for (String candidate : candidates) {
            bySpelling.put(candidate.toLowerCase(Locale.ROOT), candidate);
        }

        Set<String> stated = new HashSet<>();
        for (String line : cardText.split("\n", -1)) {
            String[] segments = keywordSegments(line);
            String firstKeyword = matchingKeyword(segments[0], bySpelling.keySet());
            if (firstKeyword != null && isEmDashParameterizedKeyword(segments[0], firstKeyword)) {
                stated.add(bySpelling.get(firstKeyword));
                continue;
            }
            List<String> onThisLine = new ArrayList<>(segments.length);
            for (String segment : segments) {
                String keyword = matchingKeyword(segment, bySpelling.keySet());
                if (keyword == null) {
                    onThisLine = null;
                    break;
                }
                onThisLine.add(keyword);
            }
            if (onThisLine != null) {
                onThisLine.forEach(keyword -> stated.add(bySpelling.get(keyword)));
            }
        }
        return stated;
    }

    private static boolean isEmDashParameterizedKeyword(String segment, String keyword) {
        return segment.length() > keyword.length()
                && segment.charAt(keyword.length()) == '\u2014';
    }

    private static boolean allKeywords(String[] segments, Set<String> keywords) {
        for (String segment : segments) {
            if (matchingKeyword(segment, keywords) == null) {
                return false;
            }
        }
        return true;
    }

    /** The lowercased keyword heading this segment, or null when the segment is not a keyword. */
    private static String matchingKeyword(String segment, Set<String> keywords) {
        String lower = segment.toLowerCase(Locale.ROOT);
        for (String keyword : keywords) {
            // equals first, so the charAt below only runs when the segment is strictly longer
            if (lower.equals(keyword)
                    || (lower.startsWith(keyword)
                    && (lower.charAt(keyword.length()) == ' '
                    || lower.charAt(keyword.length()) == '\u2014'))) {
                return keyword;
            }
        }
        return null;
    }

    private static String[] keywordSegments(String line) {
        return line.split("[,;] ", -1);
    }

    private static String capitalizeEach(String value) {
        StringBuilder line = new StringBuilder(value);
        line.setCharAt(0, Character.toUpperCase(line.charAt(0)));
        for (int i = 2; i < line.length(); i++) {
            if (line.charAt(i - 1) == ' '
                    && (line.charAt(i - 2) == ',' || line.charAt(i - 2) == ';')) {
                line.setCharAt(i, Character.toUpperCase(line.charAt(i)));
            }
        }
        return line.toString();
    }
}
