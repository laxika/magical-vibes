package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedSelfReturnFromGraveyardEffect;

@CardRegistration(set = "M15", collectorNumber = "191")
public class Phytotitan extends Card {

    public Phytotitan() {
        addEffect(EffectSlot.ON_DEATH, RegisterDelayedSelfReturnFromGraveyardEffect.tappedAtOwnersNextUpkeep());
    }
}
