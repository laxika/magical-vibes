package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.DevotionToChosenColor;
import com.github.laxika.magicalvibes.model.effect.ChooseColorAtResolutionEffect;
import com.github.laxika.magicalvibes.model.effect.ClearChosenColorEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardLoseLifeEqualToManaValueAndMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentThenEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "WHO", collectorNumber = "584")
public class HotelOfFears extends Card {

    private static final PermanentAllOfPredicate ANOTHER_CREATURE = new PermanentAllOfPredicate(List.of(
            new PermanentIsCreaturePredicate(),
            new PermanentNotPredicate(new PermanentIsSourceCardPredicate())));

    public HotelOfFears() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new ExileTopCardLoseLifeEqualToManaValueAndMayPlayThisTurnEffect());

        PutCounterOnTargetPermanentEffect counterEffect = new PutCounterOnTargetPermanentEffect(
                CounterType.PLUS_ONE_PLUS_ONE,
                new DevotionToChosenColor(),
                null,
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentControlledBySourceControllerPredicate())),
                false,
                null);

        target(TargetFilters.creatureYouControl()).addEffect(EffectSlot.CHAOS_TRIGGERED,
                SequenceEffect.of(
                        new ChooseColorAtResolutionEffect(),
                        counterEffect,
                        new SacrificePermanentThenEffect(ANOTHER_CREATURE, null, "another creature"),
                        new ClearChosenColorEffect()));
    }
}
