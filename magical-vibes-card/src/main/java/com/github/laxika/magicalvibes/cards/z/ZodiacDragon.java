package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToOwnerHandEffect;

@CardRegistration(set = "PTK", collectorNumber = "131")
public class ZodiacDragon extends Card {

    public ZodiacDragon() {
        // When this creature is put into your graveyard from the battlefield, you may return it to your hand.
        addEffect(EffectSlot.ON_DEATH,
                new MayEffect(new ReturnSourceCardFromGraveyardToOwnerHandEffect(), "Return Zodiac Dragon to your hand?"));
    }
}
