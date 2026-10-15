package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MatchingCreaturesMustAttackEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "C18", collectorNumber = "46")
public class ThantisTheWarweaver extends Card {

    public ThantisTheWarweaver() {
        addEffect(EffectSlot.STATIC, new MatchingCreaturesMustAttackEffect(
                new PermanentIsCreaturePredicate()));
        addEffect(EffectSlot.ON_CREATURE_ATTACKS_YOU, new PutCountersOnSourceEffect(1, 1, 1));
    }
}
