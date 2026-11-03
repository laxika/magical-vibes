package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileTargetSpellUntilSourceLeavesEffect;
import com.github.laxika.magicalvibes.model.condition.SourceExiledCardsThreshold;
import com.github.laxika.magicalvibes.model.effect.MayCastCardExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryNotPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryControlledByPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "OTC", collectorNumber = "16")
@CardRegistration(set = "OTC", collectorNumber = "52")
public class SmirkingSpelljacker extends Card {

    public SmirkingSpelljacker() {
        target(new StackEntryPredicateTargetFilter(
                new StackEntryNotPredicate(new StackEntryControlledByPredicate()),
                "Target must be a spell an opponent controls."))
                .addTrigger(Triggers.enters(ObjectRelation.THIS), new ExileTargetSpellUntilSourceLeavesEffect());

        addTrigger(Triggers.attacks(),
                TriggerOptions.DEFAULT.withInterveningIf(new SourceExiledCardsThreshold(1)),
                MayCastCardExiledWithSourceEffect.eachExiledCard());
    }
}
