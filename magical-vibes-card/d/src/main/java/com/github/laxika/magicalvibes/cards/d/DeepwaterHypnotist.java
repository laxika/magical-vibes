package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BNG", collectorNumber = "35")
public class DeepwaterHypnotist extends Card {

    public DeepwaterHypnotist() {
        PermanentPredicateTargetFilter targetFilter = TargetFilters.creatureAnOpponentControls();
        target(targetFilter).addTrigger(Triggers.becomesUntapped(),
                new BoostTargetCreatureEffect(-3, 0, targetFilter.predicate()));
    }
}
