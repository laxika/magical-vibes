package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;

@CardRegistration(set = "ATQ", collectorNumber = "55")
public class Mightstone extends Card {

    public Mightstone() {
        addEffect(EffectSlot.STATIC,
                new StaticBoostEffect(1, 0, GrantScope.ALL_CREATURES, new PermanentIsAttackingPredicate()));
    }
}
