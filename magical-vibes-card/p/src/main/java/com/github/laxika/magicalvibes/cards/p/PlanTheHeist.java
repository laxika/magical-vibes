package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.condition.ControllerHandEmpty;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;

import java.util.List;

@CardRegistration(set = "OTJ", collectorNumber = "62")
public class PlanTheHeist extends Card {

    public PlanTheHeist() {
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{3}{U}"))));
        addEffect(EffectSlot.SPELL, new ConditionalEffect(new ControllerHandEmpty(), new SurveilEffect(3)));
        addEffect(EffectSlot.SPELL, new DrawCardEffect(3));
    }
}
