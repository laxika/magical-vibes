package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.GreatestCreatureTypeCountAmongControlled;
import com.github.laxika.magicalvibes.model.effect.ChooseOneAtTriggerTimeEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;

import java.util.List;

@CardRegistration(set = "KHM", collectorNumber = "110")
public class SkemfarShadowsage extends Card {

    public SkemfarShadowsage() {
        GreatestCreatureTypeCountAmongControlled count = new GreatestCreatureTypeCountAmongControlled();
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseOneAtTriggerTimeEffect(new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Each opponent loses X life.",
                        new LoseLifeEffect(count, LoseLifeRecipient.EACH_OPPONENT)),
                new ChooseOneEffect.ChooseOneOption(
                        "You gain X life.",
                        new GainLifeEffect(count))
        ))));
    }
}
