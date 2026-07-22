package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;

@CardRegistration(set = "INR", collectorNumber = "110")
public class GhoulishProcession extends Card {

    public GhoulishProcession() {
        // Whenever one or more nontoken creatures die, create a 2/2 black Zombie creature token
        // with decayed. This ability triggers only once each turn.
        addEffect(EffectSlot.ON_ANY_NONTOKEN_CREATURE_DIES,
                new OncePerTurnTriggerEffect(CreateTokenEffect.blackZombieWithDecayed(1)));
    }
}
