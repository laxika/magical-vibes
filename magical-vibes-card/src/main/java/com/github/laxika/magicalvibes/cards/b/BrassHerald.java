package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostCreaturesOfChosenSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseSubtypeOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardsChosenSubtypeToHandRestToBottomEffect;

@CardRegistration(set = "8ED", collectorNumber = "293")
public class BrassHerald extends Card {

    public BrassHerald() {
        // As Brass Herald enters, choose a creature type.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseSubtypeOnEnterEffect());

        // When Brass Herald enters, reveal the top four cards of your library. Put all creature cards
        // of the chosen type revealed this way into your hand and the rest on the bottom in any order.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new RevealTopCardsChosenSubtypeToHandRestToBottomEffect(4));

        // Creatures of the chosen type get +1/+1 (all controllers, including Brass Herald itself).
        addEffect(EffectSlot.STATIC, new BoostCreaturesOfChosenSubtypeEffect(1, 1, null, true));
    }
}
