package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "TMT", collectorNumber = "21")
public class MightyMutanimals extends Card {

    public MightyMutanimals() {
        // When this creature enters, create a 2/2 red Mutant creature token.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CreateTokenEffect(
                "Mutant", 2, 2, CardColor.RED, List.of(CardSubtype.MUTANT), Set.of(), Set.of()));

        // Whenever another creature you control enters, put a +1/+1 counter on target creature you control.
        target(TargetFilters.creatureYouControl())
                .addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                        new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1));
    }
}
