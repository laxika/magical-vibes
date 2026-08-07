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
                stripSecondFace(face.name()),
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
                keywordsOf(face, cardText, isBackFace),
                // A back face carries no defense or watermark of its own, and no loyalty either
                // unless it is itself a planeswalker — a creature that transforms into one (Kytheon,
                // Hero of Akros) enters as its back face and needs that face's starting loyalty.
                isBackFace && !isPlaneswalker(parsed) ? null : CardDataSupport.parseInt(face.loyalty()),
                isBackFace ? null : CardDataSupport.parseInt(face.defense()),
                isBackFace ? null : blankToNull(face.watermark()));
    }

    private static boolean isPlaneswalker(TypeLineParser.ParsedTypeLine parsed) {
        return parsed.type() == CardType.PLANESWALKER || parsed.additionalTypes().contains(CardType.PLANESWALKER);
    }

    private static List<CardColor> colorsOf(RawFace face, boolean isBackFace) {
        // A transformed permanent has no mana cost, so its colour is printed as an indicator.
        if (isBackFace && !face.colorIndicator().isEmpty()) {
            return mapColors(face.colorIndicator());
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
     * <p>Both providers report a double-faced card's keywords combined across its faces, so a back
     * face has to be narrowed to what its own text states — otherwise it inherits the front's.
     * That is not cosmetic: Defender leaking onto Awoken Horror produces a creature the combat code
     * refuses to attack with. Narrowing also stops a face that <em>grants</em> a keyword
     * ("Creatures you control have flying") from claiming it, and drops Transform and Prepared for
     * free, since neither heads a keyword line of any back face's text.
     *
     * <p>Order matters: this runs on {@code cardText} <em>after</em> capitalisation, which is
     * itself driven by the full combined list. Narrowing first would change which lines capitalise.
     */
    private static Set<Keyword> keywordsOf(RawFace face, String cardText, boolean isBackFace) {
        Collection<String> stated = isBackFace
                ? OracleTextNormalizer.keywordsStatedIn(cardText, face.keywords())
                : face.keywords();

        Set<Keyword> keywords = EnumSet.noneOf(Keyword.class);
        for (String raw : stated) {
            Keyword keyword = CardDataSupport.keyword(raw);
            if (keyword != null) {
                keywords.add(keyword);
            }
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
