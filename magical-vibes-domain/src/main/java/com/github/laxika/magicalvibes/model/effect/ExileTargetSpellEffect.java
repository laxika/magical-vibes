package com.github.laxika.magicalvibes.model.effect;

import java.util.Objects;

/**
 * "Exile target spell", "exile any number of target spells": each target spell leaves the stack without being
 * countered, so "can't be countered" doesn't stop it. A copy isn't a card and ceases to exist. The step records its
 * {@code StepKind.EXILE}, so a later step reads the exiled card with {@code Subjects.cardsThisWay(EXILE)}.
 *
 * <p>Over {@code Subjects.triggeringSpell()} it is the "[that player] exiles it" of a spell-cast trigger (Possibility
 * Storm): no target is declared, and the spell is exiled only while it is still on the stack; once it has left, nothing
 * is exiled.
 *
 * @param spell           {@code Subjects.targetSpell()} (declares the harmful spell target) or
 *                        {@code Subjects.triggeringSpell()} (declares none)
 * @param trackWithSource whether the cards are exiled with the source permanent ("the exiled card" of its other
 *                        abilities: Spell Queller, Ashiok's Erasure; "all cards exiled with this enchantment":
 *                        Possibility Storm)
 */
public record ExileTargetSpellEffect(PermanentSubject spell, boolean trackWithSource) implements CardEffect {

    public ExileTargetSpellEffect {
        Objects.requireNonNull(spell, "spell");
        if (!(spell instanceof PermanentSubject.TargetSpell) && !(spell instanceof PermanentSubject.TriggeringSpell)) {
            throw new IllegalArgumentException("Exiles a target spell or the triggering spell: " + spell);
        }
    }

    public ExileTargetSpellEffect() {
        this(false);
    }

    public ExileTargetSpellEffect(boolean trackWithSource) {
        this(Subjects.targetSpell(), trackWithSource);
    }

    /** The exiled cards are tracked with the source permanent. */
    public static ExileTargetSpellEffect withSource() {
        return new ExileTargetSpellEffect(true);
    }

    /** "[That player] exiles it" of a spell-cast trigger, the card exiled with the source permanent (CR 607.2a). */
    public static ExileTargetSpellEffect triggeringSpellWithSource() {
        return new ExileTargetSpellEffect(Subjects.triggeringSpell(), true);
    }

    @Override
    public TargetSpec targetSpec() {
        return spell instanceof PermanentSubject.TargetSpell
                ? TargetSpec.harmful(TargetPredicates.spellOnStack()) : TargetSpec.NONE;
    }
}
