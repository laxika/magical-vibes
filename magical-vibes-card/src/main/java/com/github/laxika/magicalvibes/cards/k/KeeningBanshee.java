package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "RAV", collectorNumber = "92")
public class KeeningBanshee extends Card {

    public KeeningBanshee() {
        target(TargetFilters.creature()).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new BoostTargetCreatureEffect(-2, -2));
    }
}
