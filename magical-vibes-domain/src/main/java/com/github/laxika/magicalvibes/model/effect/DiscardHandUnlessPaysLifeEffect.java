package com.github.laxika.magicalvibes.model.effect;

/**
 * Punisher effect: target player discards their hand unless they pay {@code lifeCost} life.
 * The affected (target) player chooses whether to pay the life or discard their whole hand.
 * A player who can't pay the life (too little life, or life can't change) must discard.
 * Used by Tyrannize.
 *
 * @param lifeCost how much life the target player may pay to avoid discarding their hand
 */
public record DiscardHandUnlessPaysLifeEffect(int lifeCost) implements CardEffect {

    @Override
    public boolean canTargetPlayer() {
        return true;
    }
}
