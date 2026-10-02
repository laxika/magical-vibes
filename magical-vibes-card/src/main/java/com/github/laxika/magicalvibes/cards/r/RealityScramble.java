package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Retrace;
import com.github.laxika.magicalvibes.model.effect.PutTargetOnBottomThenRevealUntilSharedCardTypeToBattlefieldRestOnBottomRandomEffect;
import com.github.laxika.magicalvibes.model.filter.OwnedPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

@CardRegistration(set = "C18", collectorNumber = "25")
public class RealityScramble extends Card {

    public RealityScramble() {
        target(new OwnedPermanentPredicateTargetFilter(
                new PermanentTruePredicate(), "Target must be a permanent you own"))
                .addEffect(EffectSlot.SPELL,
                        new PutTargetOnBottomThenRevealUntilSharedCardTypeToBattlefieldRestOnBottomRandomEffect());
        addCastingOption(new Retrace());
    }
}
