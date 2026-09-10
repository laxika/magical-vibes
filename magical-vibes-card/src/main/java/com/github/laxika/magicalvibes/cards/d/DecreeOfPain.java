package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPermanentScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "SCG", collectorNumber = "64")
public class DecreeOfPain extends Card {

    public DecreeOfPain() {
        addEffect(EffectSlot.SPELL, new DestroyAllPermanentsEffect(
                new PermanentIsCreaturePredicate(),
                true,
                EachPermanentScope.ALL_PLAYERS,
                new DrawCardEffect(new EventValue()),
                false));

        addEffect(EffectSlot.ON_SELF_CYCLED, new BoostAllCreaturesEffect(-2, -2));
        addCycling("{3}{B}{B}");
    }
}
