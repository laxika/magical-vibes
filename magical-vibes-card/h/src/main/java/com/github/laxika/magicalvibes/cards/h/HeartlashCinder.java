package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.ColorManaSymbolsAmongControlledPermanents;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "EVE", collectorNumber = "56")
public class HeartlashCinder extends Card {

    public HeartlashCinder() {
        // Chroma — When this creature enters, it gets +X/+0 until end of turn, where X is the
        // number of red mana symbols in the mana costs of permanents you control.
        addTrigger(Triggers.enters(ObjectRelation.THIS), new BoostSelfEffect(
                new ColorManaSymbolsAmongControlledPermanents(ManaColor.RED, false), new Fixed(0)));
    }
}
