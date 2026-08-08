package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostEnteringCreatureEffect;

import java.util.Set;

@CardRegistration(set = "BOK", collectorNumber = "108")
public class InTheWebOfWar extends Card {

    public InTheWebOfWar() {
        // Whenever a creature you control enters, it gets +2/+0 and gains haste until end of turn.
        addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new BoostEnteringCreatureEffect(2, 0, Set.of(Keyword.HASTE)));
    }
}
