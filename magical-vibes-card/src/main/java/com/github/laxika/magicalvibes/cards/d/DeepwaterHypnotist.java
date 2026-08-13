package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "BNG", collectorNumber = "35")
public class DeepwaterHypnotist extends Card {

    public DeepwaterHypnotist() {
        PermanentPredicateTargetFilter targetFilter = TargetFilters.creatureAnOpponentControls();
        target(targetFilter).addEffect(EffectSlot.ON_SELF_BECOMES_UNTAPPED,
                new BoostTargetCreatureEffect(-3, 0, targetFilter.predicate()));
    }
}
