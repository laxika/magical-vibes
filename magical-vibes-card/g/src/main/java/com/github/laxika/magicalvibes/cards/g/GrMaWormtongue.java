package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.condition.SacrificedCardMatches;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChoosePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PlayersCantGainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import java.util.List;
import java.util.Set;

@CardRegistration(set = "SLD", collectorNumber = "734")
public class GrMaWormtongue extends Card {

    public GrMaWormtongue() {
        addStaticEffect(new PlayersCantGainLifeEffect(Subjects.eachOpponent()));

        PermanentPredicate army = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentHasSubtypePredicate(CardSubtype.ARMY)));
        ControlsPermanent controlsArmy = new ControlsPermanent(army);
        CardEffect amassOrcs = new ConditionalReplacementEffect(
                controlsArmy,
                new ConditionalEffect(
                        new SacrificedCardMatches(
                                new CardSupertypePredicate(CardSupertype.LEGENDARY), "legendary"),
                        SequenceEffect.of(
                                new CreateTokenEffect("Orc Army", 0, 0, CardColor.BLACK,
                                        List.of(CardSubtype.ORC, CardSubtype.ARMY), Set.of(), Set.of()),
                                ChoosePermanentsEffect.exactly(1, Subjects.controlledBy(Subjects.you(), army),
                                        "an Army you control"),
                                new PutCountersEffect(Subjects.thisWay(StepKind.CHOOSE),
                                        CounterType.PLUS_ONE_PLUS_ONE, 2))),
                new ConditionalEffect(
                        new SacrificedCardMatches(
                                new CardSupertypePredicate(CardSupertype.LEGENDARY), "legendary"),
                        SequenceEffect.of(
                                ChoosePermanentsEffect.exactly(1, Subjects.controlledBy(Subjects.you(), army),
                                        "an Army you control"),
                                new PutCountersEffect(Subjects.thisWay(StepKind.CHOOSE),
                                        CounterType.PLUS_ONE_PLUS_ONE, 2),
                                new GrantSubtypeEffect(CardSubtype.ORC, Subjects.thisWay(StepKind.CHOOSE)))));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new SacrificeCreatureCost(false, false, false, true),
                        new LoseLifeEffect(1, Subjects.targetPlayer()),
                        amassOrcs
                ),
                "{T}, Sacrifice another creature: Target player loses 1 life. If the sacrificed creature was "
                        + "legendary, amass Orcs 2.",
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.ANY),
                        "Target must be a player"
                )
        ));
    }
}
