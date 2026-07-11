package com.github.laxika.magicalvibes.model.effect;

/**
 * Target player chooses a creature they control; every <em>other</em> creature they control can't
 * block this turn (a one-shot effect setting {@code Permanent.cantBlockThisTurn} on the rest).
 *
 * <p>The chooser is the targeted player (not the caster) — pair on a card with a
 * {@code PlayerPredicateTargetFilter} restricting the target (e.g. to an opponent). At resolution
 * the target player picks one creature to keep able to block via a
 * {@code MultiPermanentChoiceContext.ChooseCreatureRestCantBlock}; with 0 or 1 creatures there is
 * nothing to restrict and it resolves with no choice. Used by Goblin War Cry.
 */
public record TargetPlayerChoosesCreatureRestCantBlockEffect() implements CardEffect {

    @Override
    public boolean canTargetPlayer() {
        return true;
    }
}
