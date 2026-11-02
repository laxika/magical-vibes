package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.KickedSpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "WWK", collectorNumber = "89")
public class RumblingAftershocks extends Card {

    public RumblingAftershocks() {
        addTrigger(Triggers.youCastSpell(), new MayEffect(
                new KickedSpellCastTriggerEffect(List.of(
                        new DealDamageToAnyTargetEffect(new EventValue())
                )),
                "Have Rumbling Aftershocks deal damage?"));
    }
}
