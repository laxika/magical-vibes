package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ChooseCardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.RevealCardsFromHandEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "HBG", collectorNumber = "46")
public class MindSpike extends Card {

    public MindSpike() {
        // Target opponent reveals each noncreature, nonland card in their hand. You choose a card revealed this way.
        // That player discards that card. You lose 2 life. If they didn't reveal a card this way, you draw a card.
        CardPredicate noncreatureNonland = new CardAllOfPredicate(List.of(
                new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)),
                new CardNotPredicate(new CardTypePredicate(CardType.LAND))));

        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent."))
                .addSpellEffect(RevealCardsFromHandEffect.cards(Subjects.targetPlayer(),
                        Subjects.cardsInHands(Subjects.targetPlayer(), noncreatureNonland)))
                .addSpellEffect(ChooseCardEffect.oneOf(Subjects.revealedThisWay(), "a card to discard"))
                .addSpellEffect(new DiscardCardsEffect(Subjects.cardsThisWay(StepKind.CHOOSE)))
                .addSpellEffect(new LoseLifeEffect(2, Subjects.you()))
                .addSpellEffect(new ConditionalEffect(new NotCondition(new HappenedThisWay(StepKind.REVEAL)),
                        new DrawCardEffect(1)));
    }
}
