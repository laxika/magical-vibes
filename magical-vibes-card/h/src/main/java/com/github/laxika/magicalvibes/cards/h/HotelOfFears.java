package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.DevotionToChosenColor;
import com.github.laxika.magicalvibes.model.effect.ChooseColorAtResolutionEffect;
import com.github.laxika.magicalvibes.model.effect.ClearChosenColorEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTargetCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "WHO", collectorNumber = "584")
public class HotelOfFears extends Card {

    /** "Another creature": a plane is no creature, so it means one other than the targeted creature. */
    private static final PermanentAllOfPredicate ANOTHER_CREATURE = new PermanentAllOfPredicate(List.of(
            new PermanentIsCreaturePredicate(),
            new PermanentNotPredicate(new PermanentIsTargetCardPredicate())));

    public HotelOfFears() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                SequenceEffect.of(
                        new ExileTopCardMayPlayThisTurnEffect(1, false),
                        new LoseLifeEffect(AmountThisWay.exiledManaValue(), Subjects.you())));

        PutCounterOnTargetPermanentEffect counterEffect = new PutCounterOnTargetPermanentEffect(
                CounterType.PLUS_ONE_PLUS_ONE,
                new DevotionToChosenColor(),
                null,
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentControlledBySourceControllerPredicate())),
                false,
                null);

        target(TargetFilters.creatureYouControl()).addTrigger(Triggers.chaosEnsues(),
                SequenceEffect.of(
                        new ChooseColorAtResolutionEffect(),
                        counterEffect,
                        new SacrificePermanentsEffect(1, ANOTHER_CREATURE, Subjects.you()),
                        new ClearChosenColorEffect()));
    }
}
