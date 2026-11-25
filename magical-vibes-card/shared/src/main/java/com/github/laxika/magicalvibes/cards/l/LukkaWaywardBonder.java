package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateEmblemEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

public class LukkaWaywardBonder extends Card {

    private static final String EMBLEM_TEXT =
            "Whenever a creature you control enters, it deals damage equal to its power to any target.";

    public LukkaWaywardBonder() {
        CardPredicate creatureCard = new CardTypePredicate(CardType.CREATURE);
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(new MayEffect(SequenceEffect.of(
                        new DiscardEffect(1, Subjects.you()),
                        new ConditionalEffect(new AllOf(List.of(HappenedThisWay.discarded(),
                                new NotCondition(HappenedThisWay.toACard(StepKind.DISCARD, creatureCard)))),
                                new DrawCardEffect(1)),
                        new ConditionalEffect(HappenedThisWay.toACard(StepKind.DISCARD, creatureCard),
                                new DrawCardEffect(2))), "Discard a card?")),
                "+1: You may discard a card. If you do, draw a card. If a creature card was discarded this way, draw two cards instead."
        ));

        addActivatedAbility(new ActivatedAbility(
                -2,
                List.of(ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.BATTLEFIELD)
                        .filter(new CardTypePredicate(CardType.CREATURE))
                        .targetGraveyard(true)
                        .grantHaste(true)
                        .exileAtNextUpkeep(true)
                        .build()),
                "−2: Return target creature card from your graveyard to the battlefield. It gains haste. Exile it at the beginning of your next upkeep."
        ));

        addActivatedAbility(new ActivatedAbility(
                -7,
                List.of(new CreateEmblemEffect(
                        new TriggeredAbility(Triggers.enters(Triggers.CREATURE, ObjectRelation.YOU_CONTROL),
                                DealDamageEffect.toAnyTarget(new SourcePower()).byTriggeringPermanent()),
                        EMBLEM_TEXT)),
                "−7: You get an emblem with \"" + EMBLEM_TEXT + "\"."
        ));
    }
}
