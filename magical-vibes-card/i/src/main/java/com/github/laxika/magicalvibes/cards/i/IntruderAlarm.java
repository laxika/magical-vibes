package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MatchingPermanentsDoesntUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "8ED", collectorNumber = "86")
@CardRegistration(set = "STH", collectorNumber = "34")
@CardRegistration(set = "SLD", collectorNumber = "229")
@CardRegistration(set = "WOT", collectorNumber = "21")
@CardRegistration(set = "TLE", collectorNumber = "15")
public class IntruderAlarm extends Card {

    public IntruderAlarm() {
        // Creatures don't untap during their controllers' untap steps.
        addStaticEffect(new MatchingPermanentsDoesntUntapEffect(new PermanentIsCreaturePredicate()));

        // Whenever a creature enters, untap all creatures.
        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANY),
                new UntapPermanentsEffect(TapUntapScope.ALL_CREATURES));
    }
}
