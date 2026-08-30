package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

import java.util.List;

@CardRegistration(set = "GPT", collectorNumber = "118")
public class InvokeTheFiremind extends Card {

    public InvokeTheFiremind() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Draw X cards",
                        new DrawCardEffect(new XValue())),
                new ChooseOneEffect.ChooseOneOption(
                        "Invoke the Firemind deals X damage to any target",
                        new DealDamageToAnyTargetEffect(new XValue()))
        )));
    }
}
