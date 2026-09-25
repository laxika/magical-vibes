package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseSubtypeOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardMayRevealMatchingToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForChosenSubtypeSpellsEffect;
import com.github.laxika.magicalvibes.model.filter.CardHasSourceChosenSubtypePredicate;

@CardRegistration(set = "MSC", collectorNumber = "287")
public class HeraldsHorn extends Card {

    public HeraldsHorn() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseSubtypeOnEnterEffect());
        addEffect(EffectSlot.STATIC, new ReduceCastCostForChosenSubtypeSpellsEffect(1));
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new LookAtTopCardMayRevealMatchingToHandEffect(
                        new CardHasSourceChosenSubtypePredicate(), false));
    }
}
