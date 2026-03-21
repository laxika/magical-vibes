package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TargetCreatureCantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "DOM", collectorNumber = "141")
public class SeismicShift extends Card {

    public SeismicShift() {
        // Destroy target land
        target(new PermanentPredicateTargetFilter(
                new PermanentIsLandPredicate(),
                "Target must be a land"
        )).addEffect(EffectSlot.SPELL, new DestroyTargetPermanentEffect());

        // Up to two target creatures can't block this turn
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature"
        ), 0, 2).addEffect(EffectSlot.SPELL, new TargetCreatureCantBlockThisTurnEffect());
    }
}
