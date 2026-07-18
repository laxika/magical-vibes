package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.PayManaCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;

import java.util.List;

@CardRegistration(set = "5ED", collectorNumber = "106")
@CardRegistration(set = "4ED", collectorNumber = "88")
public class PhantasmalForces extends Card {

    public PhantasmalForces() {
        // At the beginning of your upkeep, sacrifice this creature unless you pay {U}.
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new ForcedCostOrElseEffect(
                        new PayManaCost("{U}"),
                        List.of(new SacrificeSelfEffect()),
                        true));
    }
}
