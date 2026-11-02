package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "GPT", collectorNumber = "9")
public class HarrierGriffin extends Card {

    public HarrierGriffin() {
        PermanentPredicateTargetFilter creature = TargetFilters.creature();
        target(creature).addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                new TapPermanentsEffect(TapUntapScope.TARGET, creature.predicate()));
    }
}
