package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.BasicLandTypesAmongControlledLands;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

@CardRegistration(set = "INV", collectorNumber = "44")
public class WayfaringGiant extends Card {

    public WayfaringGiant() {
        // Domain — This creature gets +1/+1 for each basic land type among lands you control.
        BasicLandTypesAmongControlledLands domain = new BasicLandTypesAmongControlledLands();
        addEffect(EffectSlot.STATIC, new BoostSelfEffect(domain, domain));
    }
}
