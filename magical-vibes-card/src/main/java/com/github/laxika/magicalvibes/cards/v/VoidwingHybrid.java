package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToOwnerHandEffect;

@CardRegistration(set = "ONE", collectorNumber = "221")
public class VoidwingHybrid extends Card {

    public VoidwingHybrid() {
        addEffect(EffectSlot.GRAVEYARD_ON_CONTROLLER_PROLIFERATES,
                new ReturnSourceCardFromGraveyardToOwnerHandEffect());
    }
}
