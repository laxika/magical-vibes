package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "GTC", collectorNumber = "8")
@CardRegistration(set = "DDO", collectorNumber = "5")
public class CourtStreetDenizen extends Card {

    public CourtStreetDenizen() {
        // Whenever another white creature you control enters, tap target creature an opponent controls.
        target(TargetFilters.creatureAnOpponentControls())
                .addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL),
                        new TriggeringCardConditionalEffect(
                                new CardColorPredicate(CardColor.WHITE),
                                new TapPermanentsEffect(TapUntapScope.TARGET)));
    }
}
