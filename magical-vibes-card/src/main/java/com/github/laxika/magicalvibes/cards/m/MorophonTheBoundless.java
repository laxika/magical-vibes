package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.effect.BoostCreaturesOfChosenSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseSubtypeOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.ReduceColoredCastCostForMatchingSpellsEffect;
import com.github.laxika.magicalvibes.model.filter.CardHasSourceChosenSubtypePredicate;

@CardRegistration(set = "MH1", collectorNumber = "1")
public class MorophonTheBoundless extends Card {

    public MorophonTheBoundless() {
        // As Morophon enters, choose a creature type.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseSubtypeOnEnterEffect());

        // Spells of the chosen type you cast cost {W}{U}{B}{R}{G} less to cast.
        addEffect(EffectSlot.STATIC, new ReduceColoredCastCostForMatchingSpellsEffect(
                new CardHasSourceChosenSubtypePredicate(false),
                new ManaCost("{W}{U}{B}{R}{G}"),
                CostModificationScope.SELF));

        // Other creatures you control of the chosen type get +1/+1.
        addEffect(EffectSlot.STATIC, BoostCreaturesOfChosenSubtypeEffect.otherOwnCreatures(1, 1));
    }
}
