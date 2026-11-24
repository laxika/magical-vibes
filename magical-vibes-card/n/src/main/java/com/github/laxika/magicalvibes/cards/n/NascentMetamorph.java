package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfSubjectEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.RevealCardsFromTopUntilEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "C20", collectorNumber = "36")
public class NascentMetamorph extends Card {

    public NascentMetamorph() {
        var opponent = new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent");
        var creatureCard = new CardTypePredicate(CardType.CREATURE);
        var revealAndCopy = SequenceEffect.of(
                new RevealCardsFromTopUntilEffect(Subjects.targetPlayer(), creatureCard),
                BecomeCopyOfSubjectEffect.of(Subjects.revealedThisWay(creatureCard)).untilEndOfTurn(),
                PutCardsIntoLibraryEffect.onBottomInRandomOrder(Subjects.revealedThisWay()));
        target(opponent).addTrigger(Triggers.attacks(), revealAndCopy);
        addTrigger(Triggers.blocks(), TriggerOptions.ONE_OR_MORE, revealAndCopy);
    }
}
