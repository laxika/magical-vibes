package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsMayPlayUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedControllerSpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "KHM", collectorNumber = "229")
public class ShowdownOfTheSkalds extends Card {

    public ShowdownOfTheSkalds() {
        addEffect(EffectSlot.SAGA_CHAPTER_I, new ExileTopCardsMayPlayUntilNextTurnEffect(4));

        var targetCreatureYouControl = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentControlledBySourceControllerPredicate()));
        var targetFilter = TargetFilters.creatureYouControl();
        var counterEffect = PutCounterOnTargetPermanentEffect.withTargetRestriction(
                CounterType.PLUS_ONE_PLUS_ONE, 1, targetCreatureYouControl);
        addEffect(EffectSlot.SAGA_CHAPTER_II,
                new RegisterDelayedControllerSpellCastTriggerEffect(
                        null, List.of(counterEffect), false, targetFilter));
        addEffect(EffectSlot.SAGA_CHAPTER_III,
                new RegisterDelayedControllerSpellCastTriggerEffect(
                        null, List.of(counterEffect), false, targetFilter));
    }
}
