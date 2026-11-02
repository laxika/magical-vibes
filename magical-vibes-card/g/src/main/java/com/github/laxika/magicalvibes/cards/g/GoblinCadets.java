package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerGainsControlOfSourceCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "USG", collectorNumber = "189")
@CardRegistration(set = "CMD", collectorNumber = "125")
public class GoblinCadets extends Card {

    public GoblinCadets() {
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        )).addTrigger(Triggers.blocks(), TriggerMode.ONCE_PER_BLOCK,
                new TargetPlayerGainsControlOfSourceCreatureEffect());
        addTrigger(Triggers.becomesBlocked(),
                new TargetPlayerGainsControlOfSourceCreatureEffect());
    }
}
