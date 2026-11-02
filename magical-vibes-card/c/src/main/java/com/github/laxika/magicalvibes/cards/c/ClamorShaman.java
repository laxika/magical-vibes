package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.RiotEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "RNA", collectorNumber = "96")
public class ClamorShaman extends Card {

    public ClamorShaman() {
        addStaticEffect(new RiotEffect());

        target(TargetFilters.creatureAnOpponentControls())
                .addTrigger(Triggers.attacks(), new CantBlockThisTurnEffect(TapUntapScope.TARGET));
    }
}
