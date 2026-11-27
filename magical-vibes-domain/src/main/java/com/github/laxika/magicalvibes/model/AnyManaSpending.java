package com.github.laxika.magicalvibes.model;

/**
 * How a permission lets mana be spent to cast a spell. "Mana of any type can be spent" lets mana be spent as though it
 * were colorless mana or mana of any color, so colored mana may pay a {C} symbol too (CR 118.14). "As though it were
 * mana of any color" leaves a {C} symbol to colorless mana (CR 107.4c), since colorless is not a color.
 */
public enum AnyManaSpending {

    /** No such permission: mana is spent as it is. */
    NONE,

    /** "You may spend mana as though it were mana of any color": every symbol but {C}. */
    AS_THOUGH_ANY_COLOR,

    /** "Mana of any type can be spent": every symbol, {C} included. */
    ANY_TYPE;

    /** The spending of an "any mana" rider: {@code asThoughAnyColor} for the any-color wording, any type otherwise. */
    public static AnyManaSpending of(boolean anyMana, boolean asThoughAnyColor) {
        return !anyMana ? NONE : asThoughAnyColor ? AS_THOUGH_ANY_COLOR : ANY_TYPE;
    }

    /** Whether mana may be spent other than as it is. */
    public boolean isAny() {
        return this != NONE;
    }

    /** The wider of two permissions that both apply to one spell: a player may use either. */
    public AnyManaSpending or(AnyManaSpending other) {
        return other.ordinal() > ordinal() ? other : this;
    }
}
