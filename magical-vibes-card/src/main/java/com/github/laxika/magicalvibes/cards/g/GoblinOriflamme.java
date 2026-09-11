package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;

@CardRegistration(set = "FDN", collectorNumber = "539")
public class GoblinOriflamme extends Card {

    public GoblinOriflamme() {
        // Attacking creatures you control get +1/+0.
        addEffect(EffectSlot.STATIC,
                new StaticBoostEffect(1, 0, GrantScope.ALL_OWN_CREATURES, new PermanentIsAttackingPredicate()));
    }
}
