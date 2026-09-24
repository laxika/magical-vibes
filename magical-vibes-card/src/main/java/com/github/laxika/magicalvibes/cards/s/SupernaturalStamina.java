package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardOnDeathThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "AKH", collectorNumber = "111")
@CardRegistration(set = "AKR", collectorNumber = "126")
@CardRegistration(set = "A25", collectorNumber = "109")
@CardRegistration(set = "GN3", collectorNumber = "62")
@CardRegistration(set = "2X2", collectorNumber = "93")
@CardRegistration(set = "2XM", collectorNumber = "108")
@CardRegistration(set = "PIO", collectorNumber = "112")
@CardRegistration(set = "CMM", collectorNumber = "188")
public class SupernaturalStamina extends Card {

    public SupernaturalStamina() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(2, 0))
                .addEffect(EffectSlot.SPELL, new ReturnTargetCardOnDeathThisTurnEffect(true));
    }
}
