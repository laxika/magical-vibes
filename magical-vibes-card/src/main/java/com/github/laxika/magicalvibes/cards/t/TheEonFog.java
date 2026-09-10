package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PlayersSkipUntapStepEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;

@CardRegistration(set = "OHOP", collectorNumber = "8")
public class TheEonFog extends Card {

    public TheEonFog() {
        addEffect(EffectSlot.STATIC, new PlayersSkipUntapStepEffect());
        addEffect(EffectSlot.CHAOS_TRIGGERED, new UntapPermanentsEffect(TapUntapScope.CONTROLLED));
    }
}
