package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles the target permanent, then grants its owner permission to play (cast) that card until the
 * end of the owner's next turn.
 * <p>
 * Used by cards like Suspend Aggression: "Exile target nonland permanent ... its owner may play it
 * until the end of their next turn." The permanent's target restriction (e.g. nonland) is supplied
 * by the card's target filter.
 */
public record ExileTargetPermanentMayPlayUntilNextTurnEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetCategory.PERMANENT);
    }
}
