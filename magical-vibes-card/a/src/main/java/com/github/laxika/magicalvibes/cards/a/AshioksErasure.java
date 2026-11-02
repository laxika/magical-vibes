package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CantCastSpellsWithSameNameAsExiledCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetSpellUntilSourceLeavesEffect;
import com.github.laxika.magicalvibes.model.effect.PutAllCardsExiledWithSourceIntoOwnersHandsEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTruePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "THB", collectorNumber = "43")
@CardRegistration(set = "THB", collectorNumber = "304")
public class AshioksErasure extends Card {

    public AshioksErasure() {
        target(new StackEntryPredicateTargetFilter(
                new StackEntryTruePredicate(), "Target must be a spell."))
                .addTrigger(Triggers.enters(ObjectRelation.THIS), new ExileTargetSpellUntilSourceLeavesEffect())
                .addStaticEffect(new CantCastSpellsWithSameNameAsExiledCardEffect(true))
                .addTrigger(Triggers.leavesBattlefield(ObjectRelation.THIS),
                        new PutAllCardsExiledWithSourceIntoOwnersHandsEffect());
    }
}
