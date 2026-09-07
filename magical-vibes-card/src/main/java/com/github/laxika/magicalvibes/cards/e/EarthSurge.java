package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "GPT", collectorNumber = "84")
public class EarthSurge extends Card {

    public EarthSurge() {
        // Each land gets +2/+2 as long as it's a creature.
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(2, 2, GrantScope.ALL_LANDS,
                new PermanentIsCreaturePredicate()));
    }
}
