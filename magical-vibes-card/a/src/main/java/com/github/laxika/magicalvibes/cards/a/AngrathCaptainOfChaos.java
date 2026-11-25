package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ChoosePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import java.util.List;
import java.util.Set;

@CardRegistration(set = "WAR", collectorNumber = "227")
@CardRegistration(set = "SLD", collectorNumber = "527")
public class AngrathCaptainOfChaos extends Card {

    public AngrathCaptainOfChaos() {
        addStaticEffect(new GrantKeywordEffect(Keyword.MENACE, Subjects.creaturesYouControl()));

        PermanentPredicate army = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentHasSubtypePredicate(CardSubtype.ARMY)));
        ControlsPermanent controlsArmy = new ControlsPermanent(army);

        addActivatedAbility(new ActivatedAbility(
                -2,
                List.of(
                        new ConditionalEffect(controlsArmy,
                                SequenceEffect.of(
                                        ChoosePermanentsEffect.exactly(1, Subjects.controlledBy(Subjects.you(), army),
                                                "an Army you control"),
                                        new PutCountersEffect(Subjects.thisWay(StepKind.CHOOSE),
                                                CounterType.PLUS_ONE_PLUS_ONE, 2),
                                        new GrantSubtypeEffect(CardSubtype.ZOMBIE, Subjects.thisWay(StepKind.CHOOSE)))),
                        new ConditionalEffect(new NotCondition(controlsArmy),
                                SequenceEffect.of(
                                        new CreateTokenEffect("Zombie Army", 0, 0, CardColor.BLACK,
                                                List.of(CardSubtype.ZOMBIE, CardSubtype.ARMY), Set.of(), Set.of()),
                                        ChoosePermanentsEffect.exactly(1, Subjects.controlledBy(Subjects.you(), army),
                                                "an Army you control"),
                                        new PutCountersEffect(Subjects.thisWay(StepKind.CHOOSE),
                                                CounterType.PLUS_ONE_PLUS_ONE, 2)))
                ),
                "\u22122: Amass Zombies 2."
        ));
    }
}
