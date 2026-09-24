package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EnteringCreatureFromGraveyardConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;

@CardRegistration(set = "MH2", collectorNumber = "187")
public class BreathlessKnight extends Card {

    public BreathlessKnight() {
        // Whenever this creature or another creature you control enters from a graveyard or is cast
        // from a graveyard, put a +1/+1 counter on this creature.
        addEffect(EffectSlot.ON_SELF_OR_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new EnteringCreatureFromGraveyardConditionalEffect(new PutCountersOnSourceEffect(1, 1, 1)));
    }
}
