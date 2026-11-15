package com.github.laxika.magicalvibes.carddata.store;

import com.github.laxika.magicalvibes.carddata.CardDataSupport;
import com.github.laxika.magicalvibes.carddata.CardPrintingRegistry;
import com.github.laxika.magicalvibes.carddata.FaceOracleMapper;
import com.github.laxika.magicalvibes.carddata.RawFace;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.OracleData;

import java.util.List;
import java.util.Map;

/**
 * Turns the {@link RawFace}s a {@link SetSource} reads into what a {@link ConvertedSet} keeps. Both
 * sources convert through here, so a set is mapped by {@link FaceOracleMapper} the same way
 * whichever provider it came from, and nothing is mapped again when the set is loaded.
 *
 * <p>Whatever this class or anything it calls writes differently needs a new
 * {@link StoredSet#CONVERSION_VERSION}.
 */
public final class StoredSetConverter {

    private StoredSetConverter() {
    }

    /**
     * @param front       the printing's front face; for a split card, the whole card
     * @param back        the face {@code card_faces[1]} / side "b" describes, or null when there is none
     * @param splitHalves each half of a split or aftermath card in printed order; empty for every
     *                    other layout
     */
    public static ConvertedPrinting printing(String collectorNumber, String rarity, List<String> faceNames,
                                          RawFace front, RawFace back, List<RawFace> splitHalves,
                                          PrintVariant variant, Map<String, String> legalities) {
        OracleData frontOracle = splitHalves.isEmpty()
                ? FaceOracleMapper.toOracleData(front, false)
                : FaceOracleMapper.toSplitCardOracleData(front, splitHalves);
        OracleData backOracle = back == null ? null : FaceOracleMapper.toOracleData(back, true);
        return new ConvertedPrinting(
                new StoredPrinting(collectorNumber, rarity, faceNames, frontOracle, backOracle),
                new StoredPrintingDetails(collectorNumber, front.layout(), variant,
                        faceDetails(front, frontOracle, false),
                        back == null ? null : faceDetails(back, backOracle, true), legalities));
    }

    /**
     * @param side the face this entry is ("a", "b"), or null for a single-faced token
     */
    public static StoredToken token(String collectorNumber, String side, RawFace face) {
        return new StoredToken(collectorNumber, FaceOracleMapper.toOracleData(face, false),
                tokenImageKey(side, face));
    }

    private static StoredFaceDetails faceDetails(RawFace raw, OracleData oracle, boolean isBackFace) {
        String typeLine = firstFace(raw.typeLine());
        OracleData browse = FaceOracleMapper.toOracleData(new RawFace(
                firstFace(raw.name()), firstFace(raw.manaCost()), typeLine, raw.text(),
                raw.colors(), raw.colorIndicator(), raw.colorIdentity(), raw.power(),
                raw.toughness(), raw.loyalty(), raw.defense(), raw.keywords(), raw.watermark(),
                raw.layout(), null), isBackFace);
        return new StoredFaceDetails(browse.equals(oracle) ? null : browse, typeLine,
                raw.power(), raw.toughness());
    }

    private static String firstFace(String value) {
        return value != null && value.contains(" // ") ? value.substring(0, value.indexOf(" // ")) : value;
    }

    /**
     * The {@link CardPrintingRegistry#buildTokenKey} key a token's art is found under, for creature
     * and non-creature tokens (Treasure, Food, …) alike. Null for an emblem, and for every face of
     * a double-faced token but its front.
     */
    private static String tokenImageKey(String side, RawFace face) {
        if (side != null && !"a".equals(side)) {
            return null;
        }
        String typeLine = face.typeLine() == null ? "" : face.typeLine();
        if (typeLine.contains("Emblem")) {
            return null;
        }
        boolean isCreature = typeLine.contains("Creature");
        Integer power = isCreature ? CardDataSupport.parseInt(face.power()) : null;
        Integer toughness = isCreature ? CardDataSupport.parseInt(face.toughness()) : null;
        List<CardColor> colors = FaceOracleMapper.mapColors(face.colors());
        return CardPrintingRegistry.buildTokenKey(face.name(), power, toughness,
                colors.isEmpty() ? null : colors.get(0));
    }
}
