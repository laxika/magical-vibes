package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerGainsLifeEffect;

import java.util.List;

@CardRegistration(set = "5ED", collectorNumber = "3")
public class AlabasterPotion extends Card {

    public AlabasterPotion() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Target player gains X life",
                        new TargetPlayerGainsLifeEffect(new XValue())),
                new ChooseOneEffect.ChooseOneOption(
                        "Prevent the next X damage that would be dealt to any target this turn",
                        PreventDamageEffect.nextToTarget(new XValue()))
        )));
    }
}
