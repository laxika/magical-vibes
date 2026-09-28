package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MatchingPermanentsDoesntUntapEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtMostPredicate;

import java.util.List;

@CardRegistration(set = "INV", collectorNumber = "304")
public class JuntuStakes extends Card {

    public JuntuStakes() {
        // Creatures with power 1 or less don't untap during their controllers' untap steps.
        addEffect(EffectSlot.STATIC,
                new MatchingPermanentsDoesntUntapEffect(new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(), new PermanentPowerAtMostPredicate(1)))));
    }
}
