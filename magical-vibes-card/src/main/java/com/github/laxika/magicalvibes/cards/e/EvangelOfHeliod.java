package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.ColorManaSymbolsAmongControlledPermanents;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

@CardRegistration(set = "THS", collectorNumber = "11")
public class EvangelOfHeliod extends Card {

    public EvangelOfHeliod() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, CreateTokenEffect.whiteSoldier(
                new ColorManaSymbolsAmongControlledPermanents(ManaColor.WHITE)));
    }
}
