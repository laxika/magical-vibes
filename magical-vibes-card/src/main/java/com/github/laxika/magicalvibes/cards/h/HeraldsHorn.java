package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseSubtypeOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardMayRevealMatchingToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForChosenSubtypeSpellsEffect;
import com.github.laxika.magicalvibes.model.filter.CardHasSourceChosenSubtypePredicate;

@CardRegistration(set = "LTC", collectorNumber = "280")
public class HeraldsHorn extends Card {

    public HeraldsHorn() {
        // As this artifact enters, choose a creature type.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseSubtypeOnEnterEffect());

        // Creature spells you cast of the chosen type cost {1} less to cast.
        addEffect(EffectSlot.STATIC, new ReduceCastCostForChosenSubtypeSpellsEffect(1));

        // At the beginning of your upkeep, look at the top card of your library. If it's a creature
        // card of the chosen type, you may reveal it and put it into your hand.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new LookAtTopCardMayRevealMatchingToHandEffect(
                new CardHasSourceChosenSubtypePredicate(), false));
    }
}
