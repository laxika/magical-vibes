package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.LifeGainedThisTurn;
import com.github.laxika.magicalvibes.model.condition.GainedLifeThisTurn;
import com.github.laxika.magicalvibes.model.effect.CastSourceCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "C21", collectorNumber = "68")
public class SproutbackTrudge extends Card {

    public SproutbackTrudge() {
        addStaticEffect(new ReduceOwnCastCostEffect(new LifeGainedThisTurn(CountScope.CONTROLLER)));
        addTrigger(new TriggerCondition(Triggers.endStep(ActivePlayer.YOU), AbilityZones.GRAVEYARD),
                TriggerOptions.DEFAULT.withInterveningIf(new GainedLifeThisTurn()),
                new CastSourceCardFromGraveyardEffect());
    }
}
