package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "SLD", collectorNumber = "2087")
public class SonicTheHedgehog extends Card {

    public SonicTheHedgehog() {
        var flashOrHaste = new PermanentAnyOfPredicate(List.of(
                new PermanentHasKeywordPredicate(Keyword.FLASH),
                new PermanentHasKeywordPredicate(Keyword.HASTE)));
        var creatureWithFlashOrHaste = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(), flashOrHaste));

        addEffect(EffectSlot.ON_ATTACK, new PutCounterOnEachControlledPermanentEffect(
                CounterType.PLUS_ONE_PLUS_ONE, 1, creatureWithFlashOrHaste));
        addEffect(EffectSlot.ON_ANY_CREATURE_DEALT_DAMAGE, new TriggeringPermanentConditionalEffect(
                new PermanentAllOfPredicate(List.of(
                        new PermanentControlledBySourceControllerPredicate(), creatureWithFlashOrHaste)),
                CreateTokenEffect.ofTreasureToken(1, true)));
    }
}
