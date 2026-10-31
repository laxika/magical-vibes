package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EachPlayerRevealsTopCardsLandsToBattlefieldTappedRestExiledEffect;

@CardRegistration(set = "MMQ", collectorNumber = "235")
public class ClearTheLand extends Card {

    public ClearTheLand() {
        addSpellEffect(new EachPlayerRevealsTopCardsLandsToBattlefieldTappedRestExiledEffect(5));
    }
}
