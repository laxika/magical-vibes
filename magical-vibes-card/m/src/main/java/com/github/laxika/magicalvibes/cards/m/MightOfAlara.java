package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.BasicLandTypesAmongControlledLands;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "CON", collectorNumber = "85")
public class MightOfAlara extends Card {

    public MightOfAlara() {
        // Domain — Target creature gets +1/+1 until end of turn for each basic land type among lands you control.
        addSpellEffect(BoostEffect.target(
                new BasicLandTypesAmongControlledLands(), new BasicLandTypesAmongControlledLands()));
    }
}
