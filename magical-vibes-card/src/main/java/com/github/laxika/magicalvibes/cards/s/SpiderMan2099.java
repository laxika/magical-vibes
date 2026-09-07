package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.condition.ControllerOwnTurnCountAtMost;
import com.github.laxika.magicalvibes.model.condition.ControllerPlayedOrCastFromOutsideHandThisTurn;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

@CardRegistration(set = "SPM", collectorNumber = "150")
@CardRegistration(set = "SPM", collectorNumber = "205")
@CardRegistration(set = "SPM", collectorNumber = "216")
public class SpiderMan2099 extends Card {

    public SpiderMan2099() {
        setCastCondition(new NotCondition(new ControllerOwnTurnCountAtMost(3)));
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                new ConditionalEffect(
                        new ControllerPlayedOrCastFromOutsideHandThisTurn(),
                        new DealDamageToAnyTargetEffect(new SourcePower())));
    }
}
