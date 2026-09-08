package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

import java.util.List;

@CardRegistration(set = "VOW", collectorNumber = "172")
public class OliviasAttendants extends Card {

    public OliviasAttendants() {
        addEffect(EffectSlot.ON_SELF_DEALS_DAMAGE, CreateTokenEffect.ofBloodToken(new EventValue()));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{R}",
                List.of(new DealDamageToAnyTargetEffect(1)),
                "This creature deals 1 damage to any target."
        ));
    }
}
