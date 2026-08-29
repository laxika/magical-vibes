package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ControlEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeUnlessReturnOwnPermanentTypeToHandEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "WWK", collectorNumber = "44")
public class VaporSnare extends Card {

    public VaporSnare() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.STATIC, new ControlEnchantedCreatureEffect());
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new SacrificeUnlessReturnOwnPermanentTypeToHandEffect(CardType.LAND));
    }
}
