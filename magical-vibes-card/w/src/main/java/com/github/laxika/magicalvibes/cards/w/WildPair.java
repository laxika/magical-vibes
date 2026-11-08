package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.condition.TriggeringPermanentMatches;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCreatureWithSameTotalPowerToughnessEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentCastBySourceControllerThisTurnPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentCastFromZonePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "PLC", collectorNumber = "144")
@CardRegistration(set = "H09", collectorNumber = "30")
public class WildPair extends Card {

    public WildPair() {
        // Whenever a creature enters, if you cast it from your hand, you may search your library for a creature card
        // with the same total power and toughness and put it onto the battlefield. If you do, shuffle. The intervening
        // "if" is judged as the creature enters and again as the ability resolves (CR 603.4); the total is read as it
        // resolves (CR 608.2h).
        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER),
                TriggerOptions.DEFAULT.withInterveningIf(new TriggeringPermanentMatches(new PermanentAllOfPredicate(
                        List.of(new PermanentCastFromZonePredicate(Zone.HAND),
                                new PermanentCastBySourceControllerThisTurnPredicate())))),
                new MayEffect(new SearchLibraryForCreatureWithSameTotalPowerToughnessEffect(),
                        "Search your library for a creature card with the same total power and toughness?"));
    }
}
