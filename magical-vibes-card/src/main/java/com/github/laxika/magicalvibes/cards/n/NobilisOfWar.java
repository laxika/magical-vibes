package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;

@CardRegistration(set = "EVE", collectorNumber = "144")
public class NobilisOfWar extends Card {

    public NobilisOfWar() {
        // Attacking creatures you control get +2/+0.
        // OWN_CREATURES handles other creatures; SELF handles the source itself (no "other" in oracle text).
        addEffect(EffectSlot.STATIC,
                new StaticBoostEffect(2, 0, GrantScope.OWN_CREATURES, new PermanentIsAttackingPredicate()));
        addEffect(EffectSlot.STATIC,
                new StaticBoostEffect(2, 0, GrantScope.SELF, new PermanentIsAttackingPredicate()));
    }
}
