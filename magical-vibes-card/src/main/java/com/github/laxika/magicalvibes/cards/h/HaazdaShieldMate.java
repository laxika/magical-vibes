package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.PayManaCost;
import com.github.laxika.magicalvibes.model.effect.PreventDamageFromChosenSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;

import java.util.List;

@CardRegistration(set = "DIS", collectorNumber = "12")
public class HaazdaShieldMate extends Card {

    public HaazdaShieldMate() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new ForcedCostOrElseEffect(
                new PayManaCost("{W}{W}"),
                List.of(new SacrificeSelfEffect()),
                true));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{W}",
                List.of(PreventDamageFromChosenSourceEffect.nextDamageToYou()),
                "The next time a source of your choice would deal damage to you this turn, prevent that damage."
        ));
    }
}
