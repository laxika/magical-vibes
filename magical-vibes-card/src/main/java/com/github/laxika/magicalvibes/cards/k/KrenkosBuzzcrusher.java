package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyUpToOneNonbasicLandPerPlayerThenSearchEffect;

@CardRegistration(set = "MKM", collectorNumber = "136")
public class KrenkosBuzzcrusher extends Card {

    public KrenkosBuzzcrusher() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new DestroyUpToOneNonbasicLandPerPlayerThenSearchEffect());
    }
}
