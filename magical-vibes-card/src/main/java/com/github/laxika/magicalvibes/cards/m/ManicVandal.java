package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "M11", collectorNumber = "151")
public class ManicVandal extends Card {

    public ManicVandal() {
        target(new PermanentPredicateTargetFilter(
                new PermanentIsArtifactPredicate(),
                "Target must be an artifact"
        ))
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DestroyTargetPermanentEffect());
    }
}
