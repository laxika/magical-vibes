package com.github.laxika.magicalvibes.model.effect;

/**
 * "Target opponent mills {@code count} cards. If two nonland cards that share a color were milled
 * this way, repeat this process." (Sphinx's Tutelage)
 *
 * <p>Targets a player; the card restricts the target to an opponent with a {@code PlayerPredicateTargetFilter}. The
 * whole process mills the targeted player again each time.</p>
 */
public record MillOpponentRepeatOnSharedColorEffect(int count) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.player());
    }
}
