package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyReferencedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentReference;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TMP", collectorNumber = "127")
@CardRegistration(set = "9ED", collectorNumber = "123")
@CardRegistration(set = "8ED", collectorNumber = "125")
@CardRegistration(set = "TPR", collectorNumber = "99")
public class DeathPitsOfRath extends Card {

    public DeathPitsOfRath() {
        // Whenever a creature is dealt damage, destroy it. It can't be regenerated.
        // "It" is the damaged creature, the ability's triggering permanent (no target).
        addTrigger(Triggers.creatureDealtDamage(),
                new DestroyReferencedPermanentEffect(PermanentReference.TRIGGERING, true));
    }
}
