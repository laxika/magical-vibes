package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;

import java.util.Set;

@CardRegistration(set = "OTJ", collectorNumber = "127")
public class HellspurBrute extends Card {

    public HellspurBrute() {
        addEffect(EffectSlot.STATIC, new ReduceOwnCastCostEffect(
                new PermanentCount(
                        new PermanentHasAnySubtypePredicate(Set.of(
                                CardSubtype.ASSASSIN,
                                CardSubtype.MERCENARY,
                                CardSubtype.PIRATE,
                                CardSubtype.ROGUE,
                                CardSubtype.WARLOCK)),
                        CountScope.CONTROLLER)));
    }
}
