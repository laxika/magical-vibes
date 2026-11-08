package com.github.laxika.magicalvibes.model.effect;

/**
 * Ice Cave's "any other player may pay that spell's mana cost. If a player does, counter the spell", on a
 * {@code Triggers.playerCastsSpell().anySpell()} ability.
 * <p>
 * The spell isn't a target: its spell spec only points the generic cast collector's non-targeting entry at the
 * triggering spell. As the ability resolves, the handler reads that spell's mana cost with its chosen X (the live spell,
 * else the entry's triggering spell snapshot, so a second Ice Cave still offers the payment after the first countered
 * it; cost increases and reductions aren't part of it) and offers it, in APNAP order, to every player but the caster as
 * a {@link MayPayManaEffect} with {@link MayPayPayer#ANY_OTHER_PLAYER} and {@link CounterSpellEffect}.
 */
public record CounterUnlessOtherPlayerPaysManaCostOnSpellCastEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.spellOnStack());
    }
}
