package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachPlayerRevealsTopCardsLandsToBattlefieldTappedRestExiledEffect;

@CardRegistration(set = "MMQ", collectorNumber = "235")
public class ClearTheLand extends Card {

    public ClearTheLand() {
        addEffect(EffectSlot.SPELL, new EachPlayerRevealsTopCardsLandsToBattlefieldTappedRestExiledEffect(5));
    }
}
