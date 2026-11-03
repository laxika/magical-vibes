package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "GTC", collectorNumber = "8")
@CardRegistration(set = "DDO", collectorNumber = "5")
public class CourtStreetDenizen extends Card {

    public CourtStreetDenizen() {
        // Whenever another white creature you control enters, tap target creature an opponent controls.
        target(TargetFilters.creatureAnOpponentControls())
                .addTrigger(Triggers.enters(
                                new PermanentAllOfPredicate(List.of(
                                        Triggers.CREATURE,
                                        new PermanentColorInPredicate(Set.of(CardColor.WHITE)))),
                                ObjectRelation.ANOTHER_YOU_CONTROL),
                        new TapPermanentsEffect(TapUntapScope.TARGET));
    }
}
