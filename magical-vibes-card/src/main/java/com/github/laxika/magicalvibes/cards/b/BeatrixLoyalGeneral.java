package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AttachAnyNumberOfControlledEquipmentToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "FIN", collectorNumber = "426")
@CardRegistration(set = "FIN", collectorNumber = "554")
public class BeatrixLoyalGeneral extends Card {

    public BeatrixLoyalGeneral() {
        target(TargetFilters.creatureYouControl(), 0, 1)
                .addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED,
                        new MayEffect(
                                new AttachAnyNumberOfControlledEquipmentToTargetCreatureEffect(),
                                "Attach any number of Equipment you control to target creature you control?"));
    }
}
