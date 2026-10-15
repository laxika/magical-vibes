package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToOwnerHandEffect;

@CardRegistration(set = "FUT", collectorNumber = "150")
public class Quagnoth extends Card {

    public Quagnoth() {
        addEffect(EffectSlot.ON_SELF_DISCARDED_BY_OPPONENT,
                new ReturnSourceCardFromGraveyardToOwnerHandEffect());
    }
}
