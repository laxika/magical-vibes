package com.github.laxika.magicalvibes.model.effect;

/** Alternative spell cost paid as life equal to the spell's mana value. */
public record PayLifeEqualToSpellManaValueCost() implements AlternativeSpellCost {

    @Override
    public Kind kind() {
        return Kind.PAY_LIFE_EQUAL_TO_SPELL_MANA_VALUE;
    }
}
