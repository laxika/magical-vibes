package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.BasicLandTypesAmongControlledLands;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "INV", collectorNumber = "224")
public class WanderingStream extends Card {

    public WanderingStream() {
        addEffect(EffectSlot.SPELL, new GainLifeEffect(
                new Scaled(new BasicLandTypesAmongControlledLands(), 2)));
    }
}
