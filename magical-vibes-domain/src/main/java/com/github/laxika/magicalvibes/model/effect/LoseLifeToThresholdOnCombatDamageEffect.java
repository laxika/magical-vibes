package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.LifeLostThisTurn;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.OpponentLostLifeThisTurn;

/**
 * Emblem marker for "if that player lost less than N life this turn, they lose life equal to the
 * difference" after combat damage to an opponent. The trigger collector gates the trigger using
 * the damaged player's life-loss total, while the conditional payload re-checks the threshold when
 * the trigger resolves.
 */
public record LoseLifeToThresholdOnCombatDamageEffect(int lifeThreshold)
        implements EmblemCombatDamageTriggerEffect {

    public LoseLifeToThresholdOnCombatDamageEffect {
        if (lifeThreshold < 1) {
            throw new IllegalArgumentException("Life threshold must be positive");
        }
    }

    @Override
    public CardEffect triggeredEffect() {
        return new ConditionalEffect(
                new NotCondition(new OpponentLostLifeThisTurn(lifeThreshold)),
                new LoseLifeEffect(
                        new Sum(new Fixed(lifeThreshold),
                                new Scaled(new LifeLostThisTurn(CountScope.OPPONENTS), -1)),
                        LoseLifeRecipient.EACH_OPPONENT));
    }
}
