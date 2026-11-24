package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.TargetPower;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ChoosePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import java.util.List;

@CardRegistration(set = "MB2", collectorNumber = "307")
@CardRegistration(set = "MB2", collectorNumber = "543")
public class CommonBlackRemoval extends Card {

    public CommonBlackRemoval() {
        addSpellEffect(new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Destroy target creature, then create a Food token",
                        destroyThen(CreateTokenEffect.ofFoodToken(1)),
                        creatureTargetFilter()),
                new ChooseOneEffect.ChooseOneOption(
                        "Destroy target creature, then create a Treasure token",
                        destroyThen(CreateTokenEffect.ofTreasureToken(1)),
                        creatureTargetFilter()),
                new ChooseOneEffect.ChooseOneOption(
                        "Destroy target creature, then put a menace counter on a creature you control",
                        destroyThen(SequenceEffect.of(
                                ChoosePermanentsEffect.exactly(1, Subjects.creaturesYouControl(), "a creature you control"),
                                new PutCounterOnTargetPermanentEffect(Subjects.thisWay(StepKind.CHOOSE),
                                        CounterType.MENACE, new Fixed(1)))),
                        creatureTargetFilter()),
                new ChooseOneEffect.ChooseOneOption(
                        "Destroy target creature, then its controller mills cards equal to its power",
                        destroyThen(new MillEffect(new TargetPower(),
                                Subjects.controllerOf(Subjects.target()))),
                        creatureTargetFilter())
        )));
    }

    /** "Destroy target creature, then [rider]": the rider follows the destruction (CR 608.2c). */
    private static CardEffect destroyThen(CardEffect rider) {
        return SequenceEffect.of(new DestroyTargetPermanentEffect(), rider);
    }

    private static TargetFilter creatureTargetFilter() {
        return new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature.");
    }
}
