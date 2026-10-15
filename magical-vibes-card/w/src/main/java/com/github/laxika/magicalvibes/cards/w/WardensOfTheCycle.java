package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.Morbid;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;

import java.util.List;

@CardRegistration(set = "FDN", collectorNumber = "125")
public class WardensOfTheCycle extends Card {

    public WardensOfTheCycle() {
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, new ConditionalEffect(new Morbid(),
                new ChooseOneEffect(List.of(
                        new ChooseOneEffect.ChooseOneOption("You gain 2 life.", new GainLifeEffect(2)),
                        new ChooseOneEffect.ChooseOneOption("You draw a card and you lose 1 life.",
                                List.of(new DrawCardEffect(1), new LoseLifeEffect(1)))))));
    }
}
