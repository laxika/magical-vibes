package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.BasicLandTypesAmongControlledLands;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;

@CardRegistration(set = "APC", collectorNumber = "50")
public class PlanarDespair extends Card {

    public PlanarDespair() {
        Scaled minusDomain = new Scaled(new BasicLandTypesAmongControlledLands(), -1);
        addEffect(EffectSlot.SPELL, new BoostAllCreaturesEffect(minusDomain, minusDomain));
    }
}
