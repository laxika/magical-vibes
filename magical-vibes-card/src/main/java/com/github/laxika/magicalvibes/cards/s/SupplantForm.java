package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCreatureToHandAndCreateTokenCopyEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "FRF", collectorNumber = "54")
public class SupplantForm extends Card {

    public SupplantForm() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new ReturnTargetCreatureToHandAndCreateTokenCopyEffect());
    }
}
