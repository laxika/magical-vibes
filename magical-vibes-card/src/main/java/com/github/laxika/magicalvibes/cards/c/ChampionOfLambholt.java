package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MatchingCreaturesCantBlockMatchingCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerLessThanSourcePowerPredicate;

@CardRegistration(set = "AVR", collectorNumber = "171")
public class ChampionOfLambholt extends Card {

    public ChampionOfLambholt() {
        // Creatures with power less than this creature's power can't block creatures you control.
        addEffect(EffectSlot.STATIC, new MatchingCreaturesCantBlockMatchingCreaturesEffect(
                new PermanentPowerLessThanSourcePowerPredicate(),
                new PermanentControlledBySourceControllerPredicate(),
                "Creatures with power less than this creature's power can't block creatures you control"));

        // Whenever another creature you control enters, put a +1/+1 counter on this creature.
        addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD, new PutCountersOnSourceEffect(1, 1, 1));
    }
}
