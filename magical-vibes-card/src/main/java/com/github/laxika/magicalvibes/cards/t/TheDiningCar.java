package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerCreatesTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantStaticEffectToPlayerUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceActivatedAbilityCostEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureWithLeastToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;

import java.util.List;

@CardRegistration(set = "WHO", collectorNumber = "578")
public class TheDiningCar extends Card {

    public TheDiningCar() {
        CreateTokenEffect food = CreateTokenEffect.ofFoodToken(1);
        addEffect(EffectSlot.PLANESWALK_TO_TRIGGERED, new EachPlayerCreatesTokenEffect(food));
        addEffect(EffectSlot.UPKEEP_TRIGGERED, SequenceEffect.of(
                new SacrificeCreatureWithLeastToughnessEffect(),
                CreateTokenEffect.ofClueToken(1)));

        addEffect(EffectSlot.CHAOS_TRIGGERED, new GrantStaticEffectToPlayerUntilEndOfTurnEffect(
                new ReduceActivatedAbilityCostEffect(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsArtifactPredicate(),
                                new PermanentIsTokenPredicate(),
                                new PermanentControlledBySourceControllerPredicate())),
                        2,
                        false,
                        false)));
    }
}
