package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentThenEffect;
import com.github.laxika.magicalvibes.model.effect.EventStat;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnChosenOwnPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ThenEffectRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;

import java.util.List;

@CardRegistration(set = "MB2", collectorNumber = "307")
@CardRegistration(set = "MB2", collectorNumber = "543")
public class CommonBlackRemoval extends Card {

    public CommonBlackRemoval() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Destroy target creature, then create a Food token",
                        destroyThen(CreateTokenEffect.ofFoodToken(1), ThenEffectRecipient.CONTROLLER),
                        creatureTargetFilter()),
                new ChooseOneEffect.ChooseOneOption(
                        "Destroy target creature, then create a Treasure token",
                        destroyThen(CreateTokenEffect.ofTreasureToken(1), ThenEffectRecipient.CONTROLLER),
                        creatureTargetFilter()),
                new ChooseOneEffect.ChooseOneOption(
                        "Destroy target creature, then put a menace counter on a creature you control",
                        destroyThen(new PutCounterOnChosenOwnPermanentEffect(
                                CounterType.MENACE, new Fixed(1), new PermanentIsCreaturePredicate(), false, true),
                                ThenEffectRecipient.CONTROLLER),
                        creatureTargetFilter()),
                new ChooseOneEffect.ChooseOneOption(
                        "Destroy target creature, then its controller mills cards equal to its power",
                        destroyThen(EventStat.POWER,
                                new MillEffect(new EventValue(), MillRecipient.CONTROLLER),
                                ThenEffectRecipient.TARGET_CONTROLLER),
                        creatureTargetFilter())
        )));
    }

    private static DestroyTargetPermanentThenEffect destroyThen(
            CardEffect thenEffect,
            ThenEffectRecipient recipient) {
        return new DestroyTargetPermanentThenEffect(EventStat.NONE, thenEffect, recipient);
    }

    private static DestroyTargetPermanentThenEffect destroyThen(
            EventStat stat,
            CardEffect thenEffect,
            ThenEffectRecipient recipient) {
        return new DestroyTargetPermanentThenEffect(stat, thenEffect, recipient);
    }

    private static TargetFilter creatureTargetFilter() {
        return new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature.");
    }
}
