package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ControllerHasCityBlessing;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.AscendEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerDrawsCardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "RIX", collectorNumber = "42")
public class KumenasAwakening extends Card {

    public KumenasAwakening() {
        addStaticEffect(new AscendEffect());
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), SequenceEffect.of(
                new ConditionalEffect(new ControllerHasCityBlessing(), new DrawCardEffect()),
                new ConditionalEffect(new NotCondition(new ControllerHasCityBlessing()),
                        new EachPlayerDrawsCardEffect(1))));
    }
}
