package com.github.laxika.magicalvibes.model.effect;

/**
 * Target player reveals a card at random from their hand, then loses life equal to that card's
 * mana value. If the hand is empty there is nothing to reveal and no life is lost.
 *
 * <p>The life loss depends on the randomly-revealed card, only known at resolution, so this is a
 * single atomic effect rather than a reveal + a separate {@link LoseLifeEffect}. Used by
 * Singe-Mind Ogre.
 */
public record RevealRandomCardFromTargetPlayerHandLoseLifeEqualToManaValueEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetCategory.PLAYER);
    }
}
