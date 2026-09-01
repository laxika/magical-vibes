package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreaturesCantAttackControllerUnlessPredicateEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

@CardRegistration(set = "RAV", collectorNumber = "4")
public class BlazingArchon extends Card {

    public BlazingArchon() {
        addEffect(EffectSlot.STATIC, new CreaturesCantAttackControllerUnlessPredicateEffect(
                new PermanentNotPredicate(new PermanentTruePredicate())));
    }
}
