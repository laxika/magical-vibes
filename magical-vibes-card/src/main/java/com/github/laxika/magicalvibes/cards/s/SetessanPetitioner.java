package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.ColorManaSymbolsAmongControlledPermanents;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "THB", collectorNumber = "199")
public class SetessanPetitioner extends Card {

    public SetessanPetitioner() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new GainLifeEffect(new ColorManaSymbolsAmongControlledPermanents(ManaColor.GREEN)));
    }
}
