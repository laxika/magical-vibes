package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MH1", collectorNumber = "186")
public class TreetopAmbusher extends Card {

    public TreetopAmbusher() {
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{1}{G}"))));
        target(TargetFilters.creatureYouControl())
                .addTrigger(Triggers.attacks(), new BoostTargetCreatureEffect(1, 1));
    }
}
