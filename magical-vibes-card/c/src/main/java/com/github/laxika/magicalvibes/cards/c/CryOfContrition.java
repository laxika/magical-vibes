package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.HauntEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "GPT", collectorNumber = "46")
public class CryOfContrition extends Card {

    public CryOfContrition() {
        target(anyPlayer()).addSpellEffect(new DiscardEffect(1, Subjects.targetPlayer()));
        target(TargetFilters.creature()).addTrigger(Triggers.putIntoGraveyardWhileResolving(ObjectRelation.THIS),
                new HauntEffect());
        target(anyPlayer()).addTrigger(Triggers.dies(ObjectRelation.HAUNTED),
                new DiscardEffect(1, Subjects.targetPlayer()));
    }

    private static PlayerPredicateTargetFilter anyPlayer() {
        return new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player");
    }
}
