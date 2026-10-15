package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "GPT", collectorNumber = "9")
public class HarrierGriffin extends Card {

    public HarrierGriffin() {
        PermanentPredicateTargetFilter creature = TargetFilters.creature();
        target(creature).addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new TapPermanentsEffect(TapUntapScope.TARGET, creature.predicate()));
    }
}
