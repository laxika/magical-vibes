package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MoveDyingSourceCountersToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveAllCountersFromChosenPermanentsThenEnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleSelfFromGraveyardIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "FIC", collectorNumber = "95")
@CardRegistration(set = "FIC", collectorNumber = "185")
public class SinUnendingCataclysm extends Card {

    public SinUnendingCataclysm() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new RemoveAllCountersFromChosenPermanentsThenEnterWithCountersEffect(
                        new PermanentAnyOfPredicate(List.of(
                                new PermanentIsArtifactPredicate(),
                                new PermanentIsCreaturePredicate(),
                                new PermanentIsEnchantmentPredicate())),
                        CounterType.PLUS_ONE_PLUS_ONE, 2));

        target(TargetFilters.creatureYouControl()).addEffect(EffectSlot.ON_DEATH,
                SequenceEffect.of(
                        MoveDyingSourceCountersToTargetCreatureEffect.alwaysTriggers(true),
                        new ShuffleSelfFromGraveyardIntoLibraryEffect()));
    }
}
