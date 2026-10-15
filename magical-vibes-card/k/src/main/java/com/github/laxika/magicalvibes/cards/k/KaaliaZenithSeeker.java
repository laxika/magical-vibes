package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsRevealTwoTypesToHandThenRestEffect;

@CardRegistration(set = "M20", collectorNumber = "210")
public class KaaliaZenithSeeker extends Card {

    public KaaliaZenithSeeker() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                LookAtTopCardsRevealTwoTypesToHandThenRestEffect.angelDemonDragonToHandRestOnBottom(6));
    }
}
