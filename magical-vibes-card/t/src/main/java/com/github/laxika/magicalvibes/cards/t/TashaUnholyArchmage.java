package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PutExiledCardsIntoOwnersGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterGlobalTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.RevealCardsFromTopUntilEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TargetOpponentChoosesCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import java.util.List;

@CardRegistration(set = "HBG", collectorNumber = "75")
public class TashaUnholyArchmage extends Card {

    public TashaUnholyArchmage() {
        var creatureCard = new CardTypePredicate(CardType.CREATURE);
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(RegisterGlobalTriggeredAbilityEffect.untilYourNextTurn(
                        new TriggeredAbility(Triggers.creatureAttacksYouOrYourPlaneswalker(),
                        new PutCountersEffect(CounterType.MINUS_ONE_MINUS_ONE)))),
                "+1: Until your next turn, whenever a creature attacks you or Tasha, Unholy Archmage, put a -1/-1 counter on that creature."
        ));

        addActivatedAbility(new ActivatedAbility(
                -2,
                List.of(new TargetOpponentChoosesCardFromGraveyardEffect(
                        creatureCard,
                        List.of(
                                new GrantKeywordEffect(Keyword.WARD,
                                        Subjects.source(), Duration.PERMANENT),
                                new GrantTriggeredAbilityEffect(
                                        new TriggeredAbility(Triggers.becomesTargetOfOpponentSpellOrAbility(),
                                        new CounterUnlessPaysEffect(2)), Subjects.source())))),
                "−2: Target opponent puts a creature card of their choice from their graveyard onto the battlefield under your control. That creature gains ward {2}.",
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                        "Target must be an opponent"))
        );

        addActivatedAbility(new ActivatedAbility(
                -6,
                List.of(new RevealCardsFromTopUntilEffect(Subjects.targetPlayer(), creatureCard, new Fixed(3)),
                        new PutCardsOntoBattlefieldEffect(Subjects.revealedThisWay(creatureCard)).underControlOf(Subjects.you()),
                        new PutExiledCardsIntoOwnersGraveyardEffect(Subjects.restRevealedThisWay(creatureCard))),
                "−6: Target opponent reveals cards from the top of their library until they reveal three creature cards. Put those cards onto the battlefield under your control. That player puts the rest into their graveyard.",
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                        "Target must be an opponent"))
        );
    }
}
