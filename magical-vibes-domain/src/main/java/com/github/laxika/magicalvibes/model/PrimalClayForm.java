package com.github.laxika.magicalvibes.model;

/**
 * The three shapes Primal Clay can become as it enters the battlefield
 * ("your choice of a 3/3 artifact creature, a 2/2 artifact creature with flying, or a 1/6 Wall
 * artifact creature with defender"), a CR 614.1c "as it enters" choice. The chosen shape's power/toughness, keyword
 * and extra creature type become the permanent's copiable values (CR 707.2).
 */
public enum PrimalClayForm {

    THREE_THREE(3, 3, null, null),
    TWO_TWO_FLYING(2, 2, Keyword.FLYING, null),
    ONE_SIX_WALL(1, 6, Keyword.DEFENDER, CardSubtype.WALL);

    private final int power;
    private final int toughness;
    private final Keyword keyword;
    private final CardSubtype subtype;

    PrimalClayForm(int power, int toughness, Keyword keyword, CardSubtype subtype) {
        this.power = power;
        this.toughness = toughness;
        this.keyword = keyword;
        this.subtype = subtype;
    }

    public int power() {
        return power;
    }

    public int toughness() {
        return toughness;
    }

    /** The keyword this shape grants (flying/defender), or {@code null} for the plain 3/3. */
    public Keyword keyword() {
        return keyword;
    }

    /** The creature type this shape adds "in addition to its other types" (Wall), or {@code null}. */
    public CardSubtype subtype() {
        return subtype;
    }
}
