package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEqualToEnteringPowerPutOneOnTopRestOnBottomEffect;

@CardRegistration(set = "MOR", collectorNumber = "117")
public class CreamOfTheCrop extends Card {

    public CreamOfTheCrop() {
        // Whenever a creature you control enters, you may look at the top X cards of your library,
        // where X is that creature's power. If you do, put one of those cards on top of your library
        // and the rest on the bottom of your library in any order.
        addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new LookAtTopCardsEqualToEnteringPowerPutOneOnTopRestOnBottomEffect());
    }
}
