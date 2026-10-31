package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;

@CardRegistration(set = "NPH", collectorNumber = "64")
public class IchorExplosion extends Card {

    public IchorExplosion() {
        addCastingCost(new SacrificeCreatureCost(false, true));
        addSpellEffect(new BoostAllCreaturesEffect(
                new Scaled(new XValue(), -1), new Scaled(new XValue(), -1)));
    }
}
