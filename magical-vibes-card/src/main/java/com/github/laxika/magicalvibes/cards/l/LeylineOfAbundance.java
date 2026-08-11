package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AddManaWhenCreatureTappedForManaEffect;
import com.github.laxika.magicalvibes.model.effect.LeylineStartOnBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "M20", collectorNumber = "179")
public class LeylineOfAbundance extends Card {

    public LeylineOfAbundance() {
        addEffect(EffectSlot.ON_OPENING_HAND_REVEAL, new MayEffect(
                new LeylineStartOnBattlefieldEffect(),
                "Begin the game with Leyline of Abundance on the battlefield?"
        ));
        addEffect(EffectSlot.ON_CONTROLLER_TAPS_CREATURE_FOR_MANA,
                new AddManaWhenCreatureTappedForManaEffect(ManaColor.GREEN));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{6}{G}{G}",
                List.of(new PutCounterOnEachControlledPermanentEffect(
                        CounterType.PLUS_ONE_PLUS_ONE, 1, new PermanentIsCreaturePredicate())),
                "{6}{G}{G}: Put a +1/+1 counter on each creature you control."
        ));
    }
}
