package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;

import java.util.Set;

@CardRegistration(set = "VOW", collectorNumber = "229")
public class WolfkinOutcast extends Card {

    public WolfkinOutcast() {
        setBackFaceCard(new WeddingCrasher());

        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new ControlsPermanent(new PermanentHasAnySubtypePredicate(
                        Set.of(CardSubtype.WOLF, CardSubtype.WEREWOLF))),
                new ReduceOwnCastCostEffect(new Fixed(2))));
    }

    @Override
    public String getBackFaceClassName() {
        return "WeddingCrasher";
    }
}
