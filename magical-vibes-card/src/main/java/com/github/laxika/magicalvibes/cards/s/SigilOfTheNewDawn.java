package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTriggeringCardToOwnerHandEffect;

@CardRegistration(set = "ONS", collectorNumber = "55")
public class SigilOfTheNewDawn extends Card {

    public SigilOfTheNewDawn() {
        addEffect(EffectSlot.ON_CREATURE_PUT_INTO_CONTROLLER_GRAVEYARD_FROM_BATTLEFIELD,
                new MayPayManaEffect("{1}{W}", new ReturnTriggeringCardToOwnerHandEffect(),
                        "Pay {1}{W} to return that card to your hand?"));
    }
}
