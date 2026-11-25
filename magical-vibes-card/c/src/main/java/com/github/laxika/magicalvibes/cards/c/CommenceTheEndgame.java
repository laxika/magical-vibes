package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.CantBeCounteredEffect;
import com.github.laxika.magicalvibes.model.effect.ChoosePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
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

@CardRegistration(set = "WAR", collectorNumber = "45")
@CardRegistration(set = "DRC", collectorNumber = "38")
public class CommenceTheEndgame extends Card {

    public CommenceTheEndgame() {
        addStaticEffect(new CantBeCounteredEffect());
        addSpellEffect(new DrawCardEffect(2));

        PermanentPredicate army = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentHasSubtypePredicate(CardSubtype.ARMY)));
        ControlsPermanent controlsArmy = new ControlsPermanent(army);
        CardsInHand handSize = new CardsInHand(CountScope.CONTROLLER);

        addSpellEffect(new ConditionalEffect(controlsArmy,
                        SequenceEffect.of(
                                ChoosePermanentsEffect.exactly(1, Subjects.controlledBy(Subjects.you(), army),
                                        "an Army you control"),
                                new PutCountersEffect(Subjects.thisWay(StepKind.CHOOSE),
                                        CounterType.PLUS_ONE_PLUS_ONE, handSize),
                                new GrantSubtypeEffect(CardSubtype.ZOMBIE, Subjects.thisWay(StepKind.CHOOSE)))));
        addSpellEffect(new ConditionalEffect(new NotCondition(controlsArmy),
                        SequenceEffect.of(
                                new CreateTokenEffect(
                                        CardType.CREATURE, 1, "Zombie Army", 0, 0, CardColor.BLACK, null,
                                        List.of(CardSubtype.ZOMBIE, CardSubtype.ARMY), Set.of(), Set.of(),
                                        false, false, List.of(), List.of(), false, false, false, 0, Set.of()),
                                ChoosePermanentsEffect.exactly(1, Subjects.controlledBy(Subjects.you(), army),
                                        "an Army you control"),
                                new PutCountersEffect(Subjects.thisWay(StepKind.CHOOSE),
                                        CounterType.PLUS_ONE_PLUS_ONE, handSize))));
    }
}
