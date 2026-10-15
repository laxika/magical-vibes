package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.PutTargetOnBottomOfLibraryEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "10E", collectorNumber = "13")
@CardRegistration(set = "M11", collectorNumber = "11")
@CardRegistration(set = "DIS", collectorNumber = "8")
@CardRegistration(set = "TD2", collectorNumber = "22")
@CardRegistration(set = "DDL", collectorNumber = "17")
@CardRegistration(set = "SPG", collectorNumber = "74")
@CardRegistration(set = "RVR", collectorNumber = "14")
@CardRegistration(set = "RVR", collectorNumber = "304")
@CardRegistration(set = "C14", collectorNumber = "69")
@CardRegistration(set = "C17", collectorNumber = "58")
@CardRegistration(set = "SCD", collectorNumber = "15")
@CardRegistration(set = "ZNC", collectorNumber = "12")
public class Condemn extends Card {

    public Condemn() {
        target(TargetFilters.attackingCreature())
                .addEffect(EffectSlot.SPELL, new PutTargetOnBottomOfLibraryEffect())
                .addEffect(EffectSlot.SPELL, new GainLifeEffect(new EventValue(), GainLifeRecipient.TARGET_CONTROLLER));
    }
}
