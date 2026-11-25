package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.ChoosePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import java.util.List;
import java.util.Set;

@CardRegistration(set = "WAR", collectorNumber = "130")
public class GrimInitiate extends Card {

    public GrimInitiate() {
        PermanentPredicate army = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentHasSubtypePredicate(CardSubtype.ARMY)));
        ControlsPermanent controlsArmy = new ControlsPermanent(army);

        addTrigger(Triggers.dies(ObjectRelation.THIS), new ConditionalReplacementEffect(
                controlsArmy,
                SequenceEffect.of(
                        new CreateTokenEffect(
                                CardType.CREATURE, 1, "Zombie Army", 0, 0, CardColor.BLACK, null,
                                List.of(CardSubtype.ZOMBIE, CardSubtype.ARMY), Set.of(), Set.of(),
                                false, false, List.of(), List.of(), false, false, false, 0, Set.of()),
                        ChoosePermanentsEffect.exactly(1, Subjects.controlledBy(Subjects.you(), army),
                                "an Army you control"),
                        new PutCountersEffect(Subjects.thisWay(StepKind.CHOOSE),
                                CounterType.PLUS_ONE_PLUS_ONE, 1)),
                SequenceEffect.of(
                        ChoosePermanentsEffect.exactly(1, Subjects.controlledBy(Subjects.you(), army),
                                "an Army you control"),
                        new PutCountersEffect(Subjects.thisWay(StepKind.CHOOSE),
                                CounterType.PLUS_ONE_PLUS_ONE, 1),
                        new GrantSubtypeEffect(CardSubtype.ZOMBIE, Subjects.thisWay(StepKind.CHOOSE)))));
    }
}
