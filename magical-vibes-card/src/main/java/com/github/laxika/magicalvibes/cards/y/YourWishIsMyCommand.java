package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayCastInstantOrSorceryCardsFromOutsideGameEffect;

@CardRegistration(set = "MB2", collectorNumber = "304")
@CardRegistration(set = "MB2", collectorNumber = "540")
public class YourWishIsMyCommand extends Card {

    public YourWishIsMyCommand() {
        addEffect(EffectSlot.SPELL, new MayCastInstantOrSorceryCardsFromOutsideGameEffect());
    }
}
