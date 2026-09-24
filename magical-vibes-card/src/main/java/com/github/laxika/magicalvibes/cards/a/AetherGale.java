package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "E02", collectorNumber = "7")
@CardRegistration(set = "SOC", collectorNumber = "186")
public class AetherGale extends Card {

    public AetherGale() {
        target(TargetFilters.nonlandPermanent(), 6, 6)
                .addEffect(EffectSlot.SPELL, ReturnToHandEffect.target());
    }
}
