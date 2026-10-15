package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.ColorManaSymbolsAmongControlledPermanents;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

@CardRegistration(set = "EVE", collectorNumber = "56")
public class HeartlashCinder extends Card {

    public HeartlashCinder() {
        // Chroma — When this creature enters, it gets +X/+0 until end of turn, where X is the
        // number of red mana symbols in the mana costs of permanents you control.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new BoostSelfEffect(
                new ColorManaSymbolsAmongControlledPermanents(ManaColor.RED), new Fixed(0)));
    }
}
