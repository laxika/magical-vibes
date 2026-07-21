package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MustBlockSourceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "ARB", collectorNumber = "137")
public class GiantAmbushBeetle extends Card {

    public GiantAmbushBeetle() {
        // Haste is loaded from Scryfall. ETB: "you may have target creature block it this turn if able."
        // MustBlockSourceEffect(null) snapshots its source (this beetle) from the ETB stack entry.
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature"
        ))
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new MayEffect(new MustBlockSourceEffect(null),
                                "Have target creature block Giant Ambush Beetle this turn if able?"));
    }
}
