package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.CameUnderControlThisTurn;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DestroySelfAtEndStepEffect;

import java.util.List;

@CardRegistration(set = "SUM", collectorNumber = "272")
public class RocketLauncher extends Card {

    public RocketLauncher() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(
                        new DealDamageToAnyTargetEffect(1),
                        new DestroySelfAtEndStepEffect()
                ),
                "{2}: This artifact deals 1 damage to any target. Destroy this artifact at the beginning "
                        + "of the next end step. Activate only if you've controlled this artifact "
                        + "continuously since the beginning of your most recent turn."
        ).withActivationCondition(
                new NotCondition(new CameUnderControlThisTurn()),
                "Activate only if you've controlled this artifact continuously since the beginning of "
                        + "your most recent turn."));
    }
}
