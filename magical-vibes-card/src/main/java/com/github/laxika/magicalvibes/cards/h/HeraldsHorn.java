package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseSubtypeOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardMayRevealMatchingToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForMatchingSpellsEffect;
import com.github.laxika.magicalvibes.model.filter.CardHasSourceChosenSubtypePredicate;

@CardRegistration(set = "40K", collectorNumber = "241")
public class HeraldsHorn extends Card {

    public HeraldsHorn() {
        CardHasSourceChosenSubtypePredicate chosenType = new CardHasSourceChosenSubtypePredicate();

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseSubtypeOnEnterEffect());
        addEffect(EffectSlot.STATIC,
                new ReduceCastCostForMatchingSpellsEffect(chosenType, 1, CostModificationScope.SELF));
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new LookAtTopCardMayRevealMatchingToHandEffect(chosenType, false));
    }
}
