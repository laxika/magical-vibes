package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerGainsLifeEffect;

import java.util.List;

@CardRegistration(set = "5ED", collectorNumber = "33")
@CardRegistration(set = "8ED", collectorNumber = "22")
@CardRegistration(set = "7ED", collectorNumber = "18")
@CardRegistration(set = "6ED", collectorNumber = "22")
public class HealingSalve extends Card {

    public HealingSalve() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Target player gains 3 life",
                        new TargetPlayerGainsLifeEffect(3)),
                new ChooseOneEffect.ChooseOneOption(
                        "Prevent the next 3 damage that would be dealt to any target this turn",
                        PreventDamageEffect.nextToTarget(3))
        )));
    }
}
