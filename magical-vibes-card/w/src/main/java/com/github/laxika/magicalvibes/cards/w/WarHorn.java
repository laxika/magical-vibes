package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;

@CardRegistration(set = "ORI", collectorNumber = "243")
public class WarHorn extends Card {

    public WarHorn() {
        // Attacking creatures you control get +1/+0.
        addStaticEffect(new StaticBoostEffect(1, 0, GrantScope.ALL_OWN_CREATURES, new PermanentIsAttackingPredicate()));
    }
}
