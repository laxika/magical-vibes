package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.condition.ControllerOwnTurnCountAtMost;
import com.github.laxika.magicalvibes.model.condition.ControllerPlayedOrCastFromOutsideHandThisTurn;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SPM", collectorNumber = "150")
@CardRegistration(set = "SPM", collectorNumber = "205")
@CardRegistration(set = "SPM", collectorNumber = "216")
@CardRegistration(set = "OM1", collectorNumber = "157")
public class SpiderMan2099 extends Card {

    public SpiderMan2099() {
        setCastCondition(new NotCondition(new ControllerOwnTurnCountAtMost(3)));
        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                TriggerOptions.DEFAULT.withInterveningIf(new ControllerPlayedOrCastFromOutsideHandThisTurn()),
                new DealDamageToAnyTargetEffect(new SourcePower()));
    }
}
