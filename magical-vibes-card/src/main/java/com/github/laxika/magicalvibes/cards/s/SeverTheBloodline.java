package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCreatureAndAllWithSameNameEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ISD", collectorNumber = "115")
@CardRegistration(set = "INR", collectorNumber = "130")
@CardRegistration(set = "INR", collectorNumber = "389")
@CardRegistration(set = "DDQ", collectorNumber = "63")
@CardRegistration(set = "MM3", collectorNumber = "84")
@CardRegistration(set = "SIS", collectorNumber = "33")
@CardRegistration(set = "C15", collectorNumber = "136")
public class SeverTheBloodline extends Card {

    public SeverTheBloodline() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new ExileTargetCreatureAndAllWithSameNameEffect());
        addCastingOption(new FlashbackCast("{5}{B}{B}"));
    }
}
