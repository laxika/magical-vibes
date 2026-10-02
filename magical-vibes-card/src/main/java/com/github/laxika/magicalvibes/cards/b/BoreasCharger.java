package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOpponentThenSearchLandDifferenceEffect;

@CardRegistration(set = "C21", collectorNumber = "84")
@CardRegistration(set = "C18", collectorNumber = "1")
public class BoreasCharger extends Card {

    public BoreasCharger() {
        // Flying is loaded from Scryfall metadata.
        // When this creature leaves the battlefield, choose an opponent who controls more lands
        // than you. Search for Plains cards equal to the difference; one enters tapped and the rest
        // go to your hand.
        addEffect(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD,
                new ChooseOpponentThenSearchLandDifferenceEffect(CardSubtype.PLAINS));
    }
}
