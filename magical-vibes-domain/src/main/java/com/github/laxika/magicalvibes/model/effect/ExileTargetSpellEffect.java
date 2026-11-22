package com.github.laxika.magicalvibes.model.effect;

/**
 * "Exile target spell", "exile any number of target spells": each target spell leaves the stack without being
 * countered, so "can't be countered" doesn't stop it. A copy isn't a card and ceases to exist. The step records its
 * {@code StepKind.EXILE}, so a later step reads the exiled card with {@code Subjects.cardsThisWay(EXILE)}.
 *
 * @param trackWithSource whether the cards are exiled with the source permanent ("the exiled card" of its other
 *                        abilities: Spell Queller, Ashiok's Erasure)
 */
public record ExileTargetSpellEffect(boolean trackWithSource) implements CardEffect {

    public ExileTargetSpellEffect() {
        this(false);
    }

    /** The exiled cards are tracked with the source permanent. */
    public static ExileTargetSpellEffect withSource() {
        return new ExileTargetSpellEffect(true);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.spellOnStack());
    }
}
