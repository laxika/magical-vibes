package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;

@CardRegistration(set = "9ED", collectorNumber = "123")
@CardRegistration(set = "8ED", collectorNumber = "125")
public class DeathPitsOfRath extends Card {

    public DeathPitsOfRath() {
        // Whenever a creature is dealt damage, destroy it. It can't be regenerated.
        // The trigger fires for every damaged creature and queues this destroy against it.
        addEffect(EffectSlot.ON_ANY_CREATURE_DEALT_DAMAGE,
                new DestroyTargetPermanentEffect(true));
    }
}
