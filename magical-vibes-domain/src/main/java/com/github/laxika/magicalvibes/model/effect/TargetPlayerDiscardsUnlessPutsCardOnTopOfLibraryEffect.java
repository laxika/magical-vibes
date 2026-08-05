package com.github.laxika.magicalvibes.model.effect;

/**
 * "Target player discards a card unless they put a card from their hand on top of their library."
 * (Tainted Specter)
 * <p>
 * The targeted player chooses which of the two happens; either way exactly one card leaves their
 * hand. With an empty hand nothing happens at all.
 * <p>
 * The number of cards actually discarded this way (0 or 1) is recorded as the stack entry's event
 * value, so a following effect can read it with {@code EventValue} — Tainted Specter pairs this
 * with {@code MassDamageEffect(new EventValue(), true)} for its "if that player discards a card
 * this way, this creature deals 1 damage to each creature and each player" rider.
 * <p>
 * Targets a player; harmful.
 */
public record TargetPlayerDiscardsUnlessPutsCardOnTopOfLibraryEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.player());
    }
}
