package com.github.laxika.magicalvibes.carddata;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.OracleData;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The rules policy for turning a printed face into oracle data, tested once against {@link RawFace}
 * literals rather than twice through provider-shaped JSON.
 *
 * <p>Each of these rules used to be implemented once per loader and hand-synced. They had already
 * drifted — see {@link #backFaceKeepsOnlyTheKeywordsItsOwnTextStates}, which held for Scryfall and
 * not for MTGJSON, so the same card could attack under one provider and not the other.
 */
class FaceOracleMapperTest {

    private static final boolean FRONT = false;
    private static final boolean BACK = true;

    @Test
    void aFaceIsNamedByItsHalfOfADoubleFacedName() {
        OracleData data = map(face().name("Thing in the Ice // Awoken Horror"), FRONT);

        assertThat(data.name()).isEqualTo("Thing in the Ice");
    }

    @Test
    void aDoubleFacedManaCostKeepsOnlyThisFacesHalf() {
        OracleData data = map(face().manaCost("{1}{U} // {3}{U}"), FRONT);

        assertThat(data.manaCost()).isEqualTo("{1}{U}");
    }

    @Test
    void anEmptyManaCostIsNullRatherThanBlank() {
        assertThat(map(face().manaCost(""), FRONT).manaCost()).isNull();
    }

    /** A transformed permanent has no mana cost, so its colour is printed as an indicator. */
    @Test
    void backFacePrefersItsColorIndicatorOverItsColors() {
        Builder face = face().colors(List.of("W")).colorIndicator(List.of("R"));

        assertThat(map(face, BACK).colors()).containsExactly(CardColor.RED);
        // The indicator is a back-face concept; a front face reads its colors array.
        assertThat(map(face, FRONT).colors()).containsExactly(CardColor.WHITE);
    }

    /** Forest is green even though its colors array is empty. */
    @Test
    void aLandTakesItsColorFromTheCardsColorIdentity() {
        OracleData data = map(
                face().typeLine("Legendary Land").colors(List.of()).colorIdentity(List.of("U")), BACK);

        assertThat(data.color()).isEqualTo(CardColor.BLUE);
        assertThat(data.colors()).containsExactly(CardColor.BLUE);
    }

    /** Legacy Weapon is colourless with a WUBRG identity — the fallback is lands only. */
    @Test
    void aColorlessNonLandKeepsNoColorDespiteItsIdentity() {
        OracleData data = map(
                face().typeLine("Legendary Artifact").colors(List.of()).colorIdentity(List.of("W", "U")), FRONT);

        assertThat(data.color()).isNull();
        assertThat(data.colors()).isEmpty();
    }

    @Test
    void backFaceCarriesNoLoyaltyDefenseOrWatermark() {
        Builder face = face().loyalty("4").defense("3").watermark("phyrexian");

        OracleData back = map(face, BACK);
        assertThat(back.loyalty()).isNull();
        assertThat(back.defense()).isNull();
        assertThat(back.watermark()).isNull();

        OracleData front = map(face, FRONT);
        assertThat(front.loyalty()).isEqualTo(4);
        assertThat(front.defense()).isEqualTo(3);
        assertThat(front.watermark()).isEqualTo("phyrexian");
    }

    /**
     * Both providers report a double-faced card's keywords combined across faces, so a back face
     * must keep only what its own text states. Inheriting the front's Defender produced an Awoken
     * Horror the combat code refused to attack with.
     */
    @Test
    void backFaceKeepsOnlyTheKeywordsItsOwnTextStates() {
        Builder face = face()
                .text("Flying\nWhen this creature transforms, it deals 3 damage to each opponent.")
                .keywords(List.of("Flying", "Vigilance", "Transform", "Flash"));

        assertThat(map(face, BACK).keywords()).containsExactly(Keyword.FLYING);
    }

    /** A face that grants a keyword to others does not claim it for itself. */
    @Test
    void aGrantedKeywordIsNotTheFacesOwn() {
        OracleData data = map(face()
                .text("Creatures you control have flying.")
                .keywords(List.of("Flying")), BACK);

        assertThat(data.keywords()).isEmpty();
    }

    /** Transform heads no keyword line, so it falls out of a back face without a special case. */
    @Test
    void transformIsNotABackFaceKeyword() {
        OracleData data = map(face().text("Defender").keywords(List.of("Defender", "Transform")), BACK);

        assertThat(data.keywords()).containsExactly(Keyword.DEFENDER);
    }

    /** MTGJSON and Scryfall disagree on keyword casing; neither spelling may be dropped. */
    @Test
    void keywordsMapCaseInsensitively() {
        assertThat(map(face().keywords(List.of("first strike")), FRONT).keywords())
                .containsExactly(Keyword.FIRST_STRIKE);
    }

    /**
     * Capitalisation runs on the full combined list, before any narrowing — reversing the order
     * would stop keyword lines being capitalised on back faces.
     */
    @Test
    void keywordLinesAreCapitalizedBeforeKeywordsAreNarrowed() {
        OracleData data = map(face()
                .text("Flying, vigilance")
                .keywords(List.of("Flying", "Vigilance")), BACK);

        assertThat(data.cardText()).isEqualTo("Flying, Vigilance");
        assertThat(data.keywords()).containsExactlyInAnyOrder(Keyword.FLYING, Keyword.VIGILANCE);
    }

    /** Reminder text is not printed on the card. */
    @Test
    void reminderTextIsStripped() {
        OracleData data = map(face().text("Deathtouch (Any amount of damage this deals is lethal.)"), FRONT);

        assertThat(data.cardText()).isEqualTo("Deathtouch");
    }

    /** Characteristic-defining power ("*") is not a number; it must not fail the load. */
    @Test
    void nonNumericPowerBecomesZeroRatherThanThrowing() {
        OracleData data = map(face().power("*").toughness("*"), FRONT);

        assertThat(data.power()).isZero();
        assertThat(data.toughness()).isZero();
    }

    private static OracleData map(Builder face, boolean isBackFace) {
        return FaceOracleMapper.toOracleData(face.build(), isBackFace);
    }

    private static Builder face() {
        return new Builder();
    }

    /**
     * Builds a {@link RawFace} with everything defaulted, so each test states only the fields its
     * rule turns on. Lives here rather than on the record: production code always has a full face
     * from an extractor, and only a test needs a mostly-empty one.
     */
    private static final class Builder {
        private String name = "Test Card";
        private String manaCost;
        private String typeLine = "Creature — Human";
        private String text;
        private List<String> colors = List.of();
        private List<String> colorIndicator = List.of();
        private List<String> colorIdentity = List.of();
        private String power;
        private String toughness;
        private String loyalty;
        private String defense;
        private List<String> keywords = List.of();
        private String watermark;

        Builder name(String value) { this.name = value; return this; }
        Builder manaCost(String value) { this.manaCost = value; return this; }
        Builder typeLine(String value) { this.typeLine = value; return this; }
        Builder text(String value) { this.text = value; return this; }
        Builder colors(List<String> value) { this.colors = value; return this; }
        Builder colorIndicator(List<String> value) { this.colorIndicator = value; return this; }
        Builder colorIdentity(List<String> value) { this.colorIdentity = value; return this; }
        Builder power(String value) { this.power = value; return this; }
        Builder toughness(String value) { this.toughness = value; return this; }
        Builder loyalty(String value) { this.loyalty = value; return this; }
        Builder defense(String value) { this.defense = value; return this; }
        Builder keywords(List<String> value) { this.keywords = value; return this; }
        Builder watermark(String value) { this.watermark = value; return this; }

        RawFace build() {
            return new RawFace(name, manaCost, typeLine, text, colors, colorIndicator, colorIdentity,
                    power, toughness, loyalty, defense, keywords, watermark);
        }
    }
}
