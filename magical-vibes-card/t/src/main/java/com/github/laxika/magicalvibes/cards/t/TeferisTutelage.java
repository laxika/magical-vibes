package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.effect.Subjects;

/** Teferi's Tutelage — enchantment. */
@CardRegistration(set = "M21", collectorNumber = "78")
@CardRegistration(set = "M21", collectorNumber = "296")
public class TeferisTutelage extends Card {

    public TeferisTutelage() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), SequenceEffect.of(
                new DrawCardEffect(1),
                new DiscardEffect(1, Subjects.you())));
        // Whenever you draw a card, target opponent mills two cards. The opponent restriction is on the
        // card's target filter, so the trigger chooses its target when it is put on the stack.
        var trigger = target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"));
        trigger.addTrigger(Triggers.youDrawCard(), new MillEffect(2, Subjects.targetPlayer()));
    }
}
