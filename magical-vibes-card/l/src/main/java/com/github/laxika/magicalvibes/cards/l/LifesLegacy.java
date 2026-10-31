package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;

@CardRegistration(set = "M15", collectorNumber = "183")
@CardRegistration(set = "NCC", collectorNumber = "300")
public class LifesLegacy extends Card {

    public LifesLegacy() {
        // Additional cost: sacrifice a creature; its power is snapshotted into xValue.
        addCastingCost(new SacrificeCreatureCost(false, true));
        // Draw cards equal to the sacrificed creature's power.
        addSpellEffect(new DrawCardEffect(new XValue()));
    }
}
