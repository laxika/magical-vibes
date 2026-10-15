package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CanBeBlockedByAtMostNCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.EachControlledCreatureCanBeBlockedByAtMostNCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;

@CardRegistration(set = "FRF", collectorNumber = "125")
public class BattlefrontKrushok extends Card {

    public BattlefrontKrushok() {
        addEffect(EffectSlot.STATIC, new CanBeBlockedByAtMostNCreaturesEffect(1));
        addEffect(EffectSlot.STATIC, new EachControlledCreatureCanBeBlockedByAtMostNCreaturesEffect(
                1, new PermanentHasCountersPredicate(CounterType.PLUS_ONE_PLUS_ONE)));
    }
}
