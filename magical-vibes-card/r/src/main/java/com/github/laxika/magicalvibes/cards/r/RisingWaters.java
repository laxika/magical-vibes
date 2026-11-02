package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MatchingPermanentsDoesntUntapEffect;
import com.github.laxika.magicalvibes.model.effect.UntapChosenPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledByActivePlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "NEM", collectorNumber = "38")
public class RisingWaters extends Card {

    public RisingWaters() {
        // Lands don't untap during their controllers' untap steps.
        addStaticEffect(new MatchingPermanentsDoesntUntapEffect(new PermanentIsLandPredicate()));

        // At the beginning of each player's upkeep, that player untaps a land they control.
        addTrigger(Triggers.upkeep(ActivePlayer.ANY), new UntapChosenPermanentEffect(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsLandPredicate(),
                        new PermanentControlledByActivePlayerPredicate())),
                true));
    }
}
