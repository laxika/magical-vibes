package com.github.laxika.magicalvibes.model.effect;

/**
 * A non-mana alternative spell cost paid while casting a spell. Unlike ordinary {@link CostEffect}
 * values, these costs are handled by the spell-casting payment path rather than by activated
 * ability or additional-spell-cost payment.
 */
public interface AlternativeSpellCost extends CostEffect {

    enum Kind {
        PAY_LIFE_EQUAL_TO_SPELL_MANA_VALUE,
        PAY_ENERGY
    }

    Kind kind();

    default int amount() {
        throw new UnsupportedOperationException("This alternative spell cost has no fixed amount");
    }
}
