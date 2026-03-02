package com.github.laxika.magicalvibes.model.effect;

/**
 * Put one or more -1/-1 counters on target creature.
 *
 * @param count                 number of -1/-1 counters to place (default 1)
 * @param regenerateIfSurvives  if true, regenerate the creature after placing counters
 *                              provided its toughness is 1 or greater (Gore Vassal)
 */
public record PutMinusOneMinusOneCounterOnTargetCreatureEffect(int count, boolean regenerateIfSurvives) implements CardEffect {

    public PutMinusOneMinusOneCounterOnTargetCreatureEffect() {
        this(1, false);
    }

    public PutMinusOneMinusOneCounterOnTargetCreatureEffect(int count) {
        this(count, false);
    }

    @Override
    public boolean canTargetPermanent() {
        return true;
    }
}
