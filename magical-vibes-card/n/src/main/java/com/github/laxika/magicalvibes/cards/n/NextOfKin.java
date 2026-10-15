package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutLesserManaValueCreatureFromHandOrCommandZoneAndReturnAuraEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "NCC", collectorNumber = "62")
@CardRegistration(set = "NCC", collectorNumber = "162")
public class NextOfKin extends Card {

    public NextOfKin() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD,
                        new PutLesserManaValueCreatureFromHandOrCommandZoneAndReturnAuraEffect());
    }
}
