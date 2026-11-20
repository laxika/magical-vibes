package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SNC", collectorNumber = "115")
public class PlasmaJockey extends Card {

    public PlasmaJockey() {
        addCastingOption(AlternateHandCast.blitz("{2}{R}"));

        target(TargetFilters.creatureAnOpponentControls())
                .addTrigger(Triggers.attacks(), new CantBlockThisTurnEffect(Subjects.target()));
    }
}
