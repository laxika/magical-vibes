package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MatchingCreaturesCantBlockMatchingCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerLessThanSourcePowerPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "AVR", collectorNumber = "171")
@CardRegistration(set = "SLD", collectorNumber = "2245")
@CardRegistration(set = "2XM", collectorNumber = "156")
@CardRegistration(set = "MOC", collectorNumber = "293")
@CardRegistration(set = "NCC", collectorNumber = "284")
@CardRegistration(set = "MIC", collectorNumber = "136")
@CardRegistration(set = "NEC", collectorNumber = "115")
@CardRegistration(set = "C16", collectorNumber = "144")
@CardRegistration(set = "SCD", collectorNumber = "174")
@CardRegistration(set = "CM2", collectorNumber = "134")
public class ChampionOfLambholt extends Card {

    public ChampionOfLambholt() {
        // Creatures with power less than this creature's power can't block creatures you control.
        addStaticEffect(new MatchingCreaturesCantBlockMatchingCreaturesEffect(
                new PermanentPowerLessThanSourcePowerPredicate(),
                new PermanentControlledBySourceControllerPredicate(),
                "Creatures with power less than this creature's power can't block creatures you control"));

        // Whenever another creature you control enters, put a +1/+1 counter on this creature.
        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL),
                new PutCountersOnSourceEffect(1, 1, 1));
    }
}
