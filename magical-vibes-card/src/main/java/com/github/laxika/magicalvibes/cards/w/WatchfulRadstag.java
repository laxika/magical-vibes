package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfSourceEffect;

/**
 * Evolve is keyword-driven; the self-evolve trigger creates a token copy of this creature.
 */
@CardRegistration(set = "PIP", collectorNumber = "87")
@CardRegistration(set = "PIP", collectorNumber = "335")
@CardRegistration(set = "PIP", collectorNumber = "615")
@CardRegistration(set = "PIP", collectorNumber = "863")
public class WatchfulRadstag extends Card {

    public WatchfulRadstag() {
        addEffect(EffectSlot.ON_SELF_EVOLVES, new CreateTokenCopyOfSourceEffect());
    }
}
