package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;

@CardRegistration(set = "TLA", collectorNumber = "75")
public class TigerSeal extends Card {

    public TigerSeal() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new TapPermanentsEffect(TapUntapScope.SELF));
        addEffect(EffectSlot.ON_CONTROLLER_DRAWS_SECOND_CARD,
                new UntapPermanentsEffect(TapUntapScope.SELF));
    }
}
