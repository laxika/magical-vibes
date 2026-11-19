package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

@CardRegistration(set = "M12", collectorNumber = "108")
@CardRegistration(set = "TSP", collectorNumber = "131")
@CardRegistration(set = "DDR", collectorNumber = "61")
@CardRegistration(set = "TSR", collectorNumber = "139")
@CardRegistration(set = "SOA", collectorNumber = "33")
public class Smallpox extends Card {

    public Smallpox() {
        // The four steps happen in order for all players: life loss, discard, creature
        // sacrifice, then land sacrifice.
        addSpellEffect(new LoseLifeEffect(1, Subjects.eachPlayer()));
        addSpellEffect(new DiscardEffect(1, Subjects.eachPlayer()));
        addSpellEffect(new SacrificePermanentsEffect(1, new PermanentIsCreaturePredicate(), Subjects.eachPlayer()));
        addSpellEffect(new SacrificePermanentsEffect(1, new PermanentIsLandPredicate(), Subjects.eachPlayer()));
    }
}
