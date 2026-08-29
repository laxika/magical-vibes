package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedSelfReturnFromGraveyardEffect;

@CardRegistration(set = "SOI", collectorNumber = "249")
public class PrizedAmalgam extends Card {

    public PrizedAmalgam() {
        // When a creature enters from this graveyard or is cast from it, return this card tapped at
        // the beginning of the next end step.
        addEffect(EffectSlot.GRAVEYARD_ON_CREATURE_ENTERS_FROM_GRAVEYARD_OR_CAST_FROM_GRAVEYARD,
                new RegisterDelayedSelfReturnFromGraveyardEffect(null, 0, false, true));
    }
}
