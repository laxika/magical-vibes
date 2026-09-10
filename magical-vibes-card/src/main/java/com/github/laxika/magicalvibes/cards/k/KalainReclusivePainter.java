package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ControlledPermanentsEnterWithAdditionalCountersForTreasureManaEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "AFR", collectorNumber = "225")
public class KalainReclusivePainter extends Card {

    public KalainReclusivePainter() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, CreateTokenEffect.ofTreasureToken(1));
        addEffect(EffectSlot.STATIC, new ControlledPermanentsEnterWithAdditionalCountersForTreasureManaEffect(
                new PermanentIsCreaturePredicate()));
    }
}
