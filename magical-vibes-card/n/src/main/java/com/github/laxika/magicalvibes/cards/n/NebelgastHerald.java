package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;


@CardRegistration(set = "INR", collectorNumber = "78")
@CardRegistration(set = "INR", collectorNumber = "364")
@CardRegistration(set = "EMN", collectorNumber = "71")
@CardRegistration(set = "SIR", collectorNumber = "83")
@CardRegistration(set = "VOC", collectorNumber = "109")
public class NebelgastHerald extends Card {

    public NebelgastHerald() {
        // Flash and Flying are loaded from Scryfall.

        // Whenever this creature or another Spirit you control enters, tap target creature an
        // opponent controls.
        target(TargetFilters.creatureAnOpponentControls())
                .addTrigger(Triggers.enters(ObjectRelation.THIS), new TapPermanentsEffect(TapUntapScope.TARGET));

        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL),
                new TriggeringCardConditionalEffect(
                new CardSubtypePredicate(CardSubtype.SPIRIT),
                new TapPermanentsEffect(TapUntapScope.TARGET)));
    }
}
