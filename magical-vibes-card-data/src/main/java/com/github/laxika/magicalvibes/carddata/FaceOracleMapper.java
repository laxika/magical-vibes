package com.github.laxika.magicalvibes.carddata;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.OracleData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Turns a {@link RawFace} into the {@link OracleData} the game plays with. Every rules decision
 * about a printed face lives here and nowhere else.
 *
 * <p>This is the whole point of {@link RawFace}: these rules used to be implemented once per
 * provider and hand-synced, and they had already drifted — back faces inherited their front face's
 * keywords on both providers, but only one had been fixed, so Awoken Horror could attack under
 * Scryfall and could not under MTGJSON. A rule written once cannot diverge.
 *
 * <p>The dividing line: a loader owns <em>provider quirks</em> (field naming, face resolution,
 * array ordering, syntax normalisation). This class owns <em>rules policy</em>. Anything a loader
 * does "to match what the other loader outputs" belongs here instead.
 */
public final class FaceOracleMapper {

    private FaceOracleMapper() {
    }

    /**
     * @param isBackFace the one thing a face cannot tell about itself, and what four of the rules
     *                   below turn on
     */
    public static OracleData toOracleData(RawFace face, boolean isBackFace) {
        String typeLine = face.typeLine() == null ? "" : face.typeLine();
        TypeLineParser.ParsedTypeLine parsed = TypeLineParser.parse(typeLine);

        List<CardColor> colors = colorsOf(face, isBackFace);
        String cardText = cardTextOf(face);

        return new OracleData(
                // A double-faced card's printed name is "Front // Back"; a face is named by its half.
                // A split card, aftermath included, keeps both names off the stack (CR 709.4a).
                isSplitLayout(face.layout()) && !isBackFace ? face.name() : stripSecondFace(face.name()),
                parsed.type(),
                parsed.additionalTypes(),
                blankToNull(stripSecondFace(face.manaCost())),
                colors.isEmpty() ? null : colors.get(0),
                colors,
                mapColors(face.colorIdentity()),
                parsed.supertypes(),
                parsed.subtypes(),
                cardText,
                CardDataSupport.parseInt(face.power()),
                CardDataSupport.parseInt(face.toughness()),
                keywordsOf(face, cardText),
                // A back face carries no defense or watermark of its own, and no loyalty either
                // unless it is itself a planeswalker — a creature that transforms into one (Kytheon,
                // Hero of Akros) enters as its back face and needs that face's starting loyalty.
                isBackFace && !isPlaneswalker(parsed) ? null : CardDataSupport.parseInt(face.loyalty()),
                isBackFace ? null : CardDataSupport.parseInt(face.defense()),
                isBackFace ? null : blankToNull(face.watermark()),
                !isBackFace && isSplitLayout(face.layout()) && face.manaValue() != null
                        ? new java.math.BigDecimal(face.manaValue()).intValueExact() : null);
    }

    /** True for a layout whose card is printed as halves cast separately (CR 709). */
    public static boolean isSplitLayout(String layout) {
        return "split".equals(layout) || "aftermath".equals(layout);
    }

    /**
     * A split card: the whole card from its front face, plus each half on its own.
     *
     * <p>Every zone but the stack sees the halves combined — the whole card has each half's card
     * types (CR 709.4c) and the colours of their combined mana cost (CR 709.4b). Providers disagree
     * on the whole card's colours (Scryfall reports both halves', MTGJSON only the front's), so the
     * union is taken here from the halves themselves. A spell cast from one half has only that
     * half's characteristics (CR 709.3b), which is what {@link OracleData#splitHalves()} carries.
     */
    public static OracleData toSplitCardOracleData(RawFace card, List<RawFace> halves) {
        OracleData whole = toOracleData(card, false);
        List<OracleData> mappedHalves = halves.stream().map(FaceOracleMapper::toSplitHalfOracleData).toList();
        if (mappedHalves.size() < 2) {
            return whole;
        }

        Set<CardType> additionalTypes = EnumSet.noneOf(CardType.class);
        additionalTypes.addAll(whole.additionalTypes());
        for (OracleData half : mappedHalves) {
            if (half.type() != null) additionalTypes.add(half.type());
            additionalTypes.addAll(half.additionalTypes());
        }
        additionalTypes.remove(whole.type());

        Set<CardColor> colorSet = EnumSet.noneOf(CardColor.class);
        colorSet.addAll(whole.colors());
        mappedHalves.forEach(half -> colorSet.addAll(half.colors()));
        List<CardColor> colors = mapColors(colorSet.stream()
                .map(FaceOracleMapper::symbolOf).sorted().toList());

        return new OracleData(whole.name(), whole.type(), Set.copyOf(additionalTypes), whole.manaCost(),
                colors.isEmpty() ? null : colors.get(0), colors, whole.colorIdentity(),
                whole.supertypes(), whole.subtypes(), whole.cardText(), whole.power(),
                whole.toughness(), whole.keywords(), whole.loyalty(), whole.defense(),
                whole.watermark(), whole.combinedManaValue(), mappedHalves);
    }

    /**
     * One half of a split card with only its own characteristics (CR 709.3b). Its colours come from
     * its own mana cost (CR 202.2) rather than from the provider, since neither provider reliably
     * reports a single half's colours.
     */
    static OracleData toSplitHalfOracleData(RawFace half) {
        TypeLineParser.ParsedTypeLine parsed = TypeLineParser.parse(half.typeLine() == null ? "" : half.typeLine());
        List<CardColor> colors = mapColors(manaCostColorSymbols(half.manaCost()));
        String cardText = cardTextOf(half);
        return new OracleData(
                half.name(),
                parsed.type(),
                parsed.additionalTypes(),
                blankToNull(half.manaCost()),
                colors.isEmpty() ? null : colors.get(0),
                colors,
                mapColors(half.colorIdentity()),
                parsed.supertypes(),
                parsed.subtypes(),
                cardText,
                null,
                null,
                keywordsOf(half, cardText),
                null,
                null,
                null);
    }

    /** The colour symbols in a mana cost, sorted the way Scryfall orders colour arrays. */
    private static List<String> manaCostColorSymbols(String manaCost) {
        if (manaCost == null) {
            return List.of();
        }
        java.util.TreeSet<String> symbols = new java.util.TreeSet<>();
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("\\{([^}]*)}").matcher(manaCost);
        while (matcher.find()) {
            for (String part : matcher.group(1).split("/")) {
                if (CardDataSupport.COLOR_MAP.containsKey(part)) {
                    symbols.add(part);
                }
            }
        }
        return List.copyOf(symbols);
    }

    private static String symbolOf(CardColor color) {
        return CardDataSupport.COLOR_MAP.entrySet().stream()
                .filter(entry -> entry.getValue() == color)
                .map(java.util.Map.Entry::getKey)
                .findFirst().orElseThrow();
    }

    private static boolean isPlaneswalker(TypeLineParser.ParsedTypeLine parsed) {
        return parsed.type() == CardType.PLANESWALKER || parsed.additionalTypes().contains(CardType.PLANESWALKER);
    }

    private static List<CardColor> colorsOf(RawFace face, boolean isBackFace) {
        // A transformed permanent has no mana cost, so its colour is printed as an indicator.
        if (isBackFace && !face.colorIndicator().isEmpty()) {
            return mapColors(face.colorIndicator());
        }

        // The second half of a split card (an aftermath half cast from the graveyard) is a back
        // face too. Like any half it is the colour of its own mana cost (CR 202.2), which Scryfall
        // does not report on a half — reading the provider's colours left it colourless. An
        // adventure's spell half (an Omen such as Charring Bite) is a separate spell with its own
        // mana cost and is coloured the same way.
        if (isBackFace && (isSplitLayout(face.layout()) || "adventure".equals(face.layout()))) {
            return mapColors(manaCostColorSymbols(face.manaCost()));
        }

        // A flip card's flipped half keeps the colour of the card, which the provider reports only on the
        // card (Tok-Tok, Volcano Born is red); the face itself reads as colourless. The card-level colour
        // identity of such a creature is its colour.
        if (isBackFace && "flip".equals(face.layout()) && face.colors().isEmpty()) {
            return mapColors(face.colorIdentity());
        }

        // CR 202.2: an object is the colour of the mana symbols in its mana cost, and nothing else.
        // A land has no mana cost, so it is colourless however its identity reads — Anarchy
        // ("destroy all white permanents") must not touch a Plains. The identity travels separately
        // as OracleData#colorIdentity, which only the view layer reads to tint a land's frame.
        return mapColors(face.colors());
    }

    /** Maps upstream colour symbols, dropping any this game has no {@link CardColor} for. */
    public static List<CardColor> mapColors(Collection<String> symbols) {
        List<CardColor> colors = new ArrayList<>(symbols.size());
        for (String symbol : symbols) {
            CardColor mapped = CardDataSupport.COLOR_MAP.get(symbol);
            if (mapped != null) {
                colors.add(mapped);
            }
        }
        return List.copyOf(colors);
    }

    private static String cardTextOf(RawFace face) {
        if (face.text() == null || face.text().isEmpty()) {
            return null;
        }
        return OracleTextNormalizer.capitalizeKeywordLines(
                OracleTextNormalizer.cleanCardText(face.text()), face.keywords());
    }

    /**
     * The face's own keywords.
     *
     * <p>Both providers report a double-faced card's keywords combined across its faces, so each
     * face has to be narrowed to what its own text states — otherwise it inherits the other face's.
     * That is not cosmetic: Defender leaking onto Awoken Horror produces a creature the combat code
     * refuses to attack with. The same filtering prevents a front face from treating a mentioned
     * keyword as its own. Narrowing also stops a face that <em>grants</em> a keyword
     * ("Creatures you control have flying") from claiming it, and drops Transform for
     * free, since it heads no keyword line of any back face's text. Prepared is never a keyword line
     * (it is stated as "enters prepared" or "becomes prepared"), so a face keeps it only when its own
     * text mentions being prepared.
     *
     * <p>Order matters: this runs on {@code cardText} <em>after</em> capitalisation, which is
     * itself driven by the full combined list. Narrowing first would change which lines capitalise.
     * With no rules text, the provider list is retained because there is no text to narrow.
     */
    private static Set<Keyword> keywordsOf(RawFace face, String cardText) {
        Collection<String> stated = cardText == null
                ? face.keywords()
                : OracleTextNormalizer.keywordsStatedIn(cardText, face.keywords());

        Set<Keyword> keywords = EnumSet.noneOf(Keyword.class);
        for (String raw : stated) {
            Keyword keyword = CardDataSupport.keyword(raw);
            if (keyword != null) {
                keywords.add(keyword);
            }
        }
        if (cardText != null
                && face.keywords().stream().anyMatch(raw -> "Prepared".equalsIgnoreCase(raw))
                && java.util.regex.Pattern.compile("(?i)\\bprepared\\b").matcher(cardText).find()) {
            keywords.add(Keyword.PREPARED);
        }
        if (cardText != null && java.util.regex.Pattern.compile("(?m)^Nimble(?:[ (]|$)")
                .matcher(cardText).find()) {
            keywords.add(Keyword.NIMBLE);
        }
        if (cardText != null && java.util.regex.Pattern.compile("(?m)^Deworded(?:[ (]|$)")
                .matcher(cardText).find()) {
            keywords.add(Keyword.DEWORDED);
        }
        if (cardText != null && java.util.regex.Pattern.compile("(?m)^Positioning(?:[ (]|$)")
                .matcher(cardText).find()) {
            keywords.add(Keyword.POSITIONING);
        }
        return keywords;
    }

    private static String stripSecondFace(String value) {
        if (value == null) {
            return null;
        }
        int separator = value.indexOf(" // ");
        return separator < 0 ? value : value.substring(0, separator);
    }

    private static String blankToNull(String value) {
        return value == null || value.isEmpty() ? null : value;
    }
}
