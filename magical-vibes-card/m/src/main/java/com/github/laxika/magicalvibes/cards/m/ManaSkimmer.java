package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.TapChosenPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TSP", collectorNumber = "117")
public class ManaSkimmer extends Card {

    public ManaSkimmer() {
        addTrigger(Triggers.dealsDamageToPlayer(),
                TapChosenPermanentEffect.damagedPlayerControlsAndSkipsNextUntap(
                        new PermanentIsLandPredicate()));
    }
}
