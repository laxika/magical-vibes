package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentMayPlayWithOpponentTaxEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentEnteredOrCastFromZonePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerLimit;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "WOE", collectorNumber = "48")
@CardRegistration(set = "WOE", collectorNumber = "331")
public class ExtraordinaryJourney extends Card {

    public ExtraordinaryJourney() {
        targetUpTo(new XValue(), TargetFilters.creature(), 100)
                .addTrigger(Triggers.enters(ObjectRelation.THIS),
                        new ExileTargetPermanentMayPlayWithOpponentTaxEffect(0));
        // "Whenever one or more nontoken creatures enter, if one or more of them entered from exile or was cast from
        // exile": a filter judged on each creature as it entered, this one included when it is a creature
        // (Opalescence). "This ability triggers only once each turn."
        addTrigger(Triggers.enters(new PermanentAllOfPredicate(List.of(Triggers.CREATURE, Triggers.NONTOKEN,
                                new PermanentEnteredOrCastFromZonePredicate(Zone.EXILE))), ObjectRelation.ANY),
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN), new DrawCardEffect(1));
    }
}
