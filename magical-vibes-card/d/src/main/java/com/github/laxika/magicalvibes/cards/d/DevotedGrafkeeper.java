package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DisturbCast;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.StackEntryCastFromZonePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MID", collectorNumber = "218")
@CardRegistration(set = "DBL", collectorNumber = "218")
public class DevotedGrafkeeper extends Card {

    public DevotedGrafkeeper() {
        setBackFaceCard(new DepartedSoulkeeper());

        // When this creature enters, mill two cards.
        addTrigger(Triggers.enters(ObjectRelation.THIS), new MillEffect(2, MillRecipient.CONTROLLER));

        // Whenever you cast a spell from your graveyard, tap target creature you don't control.
        target(TargetFilters.creatureAnOpponentControls()).addTrigger(Triggers.youCastSpell()
                        .withStackEntryFilter(new StackEntryCastFromZonePredicate(Zone.GRAVEYARD)),
                new TapPermanentsEffect(TapUntapScope.TARGET));

        // Disturb {1}{W}{U}
        addCastingOption(new DisturbCast("{1}{W}{U}"));
    }

    @Override
    public String getBackFaceClassName() {
        return "DepartedSoulkeeper";
    }
}
