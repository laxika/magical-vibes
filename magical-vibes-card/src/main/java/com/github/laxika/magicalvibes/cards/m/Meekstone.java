package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MatchingPermanentsDoesntUntapEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate;

@CardRegistration(set = "7ED", collectorNumber = "307")
@CardRegistration(set = "6ED", collectorNumber = "299")
@CardRegistration(set = "5ED", collectorNumber = "389")
@CardRegistration(set = "4ED", collectorNumber = "335")
public class Meekstone extends Card {

    public Meekstone() {
        // Creatures with power 3 or greater don't untap during their controllers' untap steps.
        addEffect(EffectSlot.STATIC,
                new MatchingPermanentsDoesntUntapEffect(new PermanentPowerAtLeastPredicate(3)));
    }
}
