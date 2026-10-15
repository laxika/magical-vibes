package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutRandomCreatureFromRandomGraveyardOntoBattlefieldAndDealManaValueDamageEffect;

@CardRegistration(set = "MB2", collectorNumber = "541")
public class CallFromTheGrave extends Card {

    public CallFromTheGrave() {
        // Put a random creature from a random graveyard onto the battlefield under your control.
        // Call from the Grave deals damage to you equal to that creature's mana value.
        addEffect(EffectSlot.SPELL,
                new PutRandomCreatureFromRandomGraveyardOntoBattlefieldAndDealManaValueDamageEffect());
    }
}
