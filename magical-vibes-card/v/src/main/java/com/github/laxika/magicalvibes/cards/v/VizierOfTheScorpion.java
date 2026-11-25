package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.Fixed;
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
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import java.util.List;
import java.util.Set;

@CardRegistration(set = "WAR", collectorNumber = "111")
@CardRegistration(set = "SCD", collectorNumber = "119")
public class VizierOfTheScorpion extends Card {

    public VizierOfTheScorpion() {
        PermanentPredicate army = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentHasSubtypePredicate(CardSubtype.ARMY)));
        ControlsPermanent controlsArmy = new ControlsPermanent(army);

        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new ConditionalEffect(controlsArmy,
                        SequenceEffect.of(
                                ChoosePermanentsEffect.exactly(1, Subjects.controlledBy(Subjects.you(), army),
                                        "an Army you control"),
                                new PutCountersEffect(Subjects.thisWay(StepKind.CHOOSE),
                                        CounterType.PLUS_ONE_PLUS_ONE, new Fixed(1)),
                                new GrantSubtypeEffect(CardSubtype.ZOMBIE, Subjects.thisWay(StepKind.CHOOSE)))),
                new ConditionalEffect(new NotCondition(controlsArmy),
                        new CreateTokenEffect(
                                CardType.CREATURE, 1, "Zombie Army", 0, 0, CardColor.BLACK, null,
                                List.of(CardSubtype.ZOMBIE, CardSubtype.ARMY), Set.of(), Set.of(),
                                false, false, List.of(), List.of(), false, false, false, 1, Set.of())));

        PermanentPredicate zombieToken = new PermanentAllOfPredicate(List.of(
                new PermanentHasSubtypePredicate(CardSubtype.ZOMBIE),
                new PermanentIsTokenPredicate()));
        addStaticEffect(new GrantKeywordEffect(Keyword.DEATHTOUCH, Subjects.creaturesYouControl(), zombieToken));
    }
}
