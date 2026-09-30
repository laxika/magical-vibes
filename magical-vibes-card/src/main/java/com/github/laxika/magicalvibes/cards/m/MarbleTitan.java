package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MatchingPermanentsDoesntUntapEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate;

import java.util.List;

@CardRegistration(set = "9ED", collectorNumber = "26")
@CardRegistration(set = "TMP", collectorNumber = "28")
public class MarbleTitan extends Card {

    public MarbleTitan() {
        // Creatures with power 3 or greater don't untap during their controllers' untap steps.
        addEffect(EffectSlot.STATIC,
                new MatchingPermanentsDoesntUntapEffect(new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(), new PermanentPowerAtLeastPredicate(3)))));
    }
}
