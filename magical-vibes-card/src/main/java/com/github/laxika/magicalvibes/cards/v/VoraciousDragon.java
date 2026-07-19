package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.DevouredCreaturesOfSubtype;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DevourEffect;

@CardRegistration(set = "CON", collectorNumber = "75")
public class VoraciousDragon extends Card {

    public VoraciousDragon() {
        // Flying is auto-loaded as a keyword from Scryfall.

        // Devour 1 (As this creature enters, you may sacrifice any number of creatures.
        // It enters with that many +1/+1 counters on it.)
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DevourEffect(1));

        // When this creature enters, it deals damage to any target equal to twice the number of
        // Goblins it devoured. (DealDamageToAnyTargetEffect declares its own any-target spec.)
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new DealDamageToAnyTargetEffect(new Scaled(new DevouredCreaturesOfSubtype(CardSubtype.GOBLIN), 2)));
    }
}
