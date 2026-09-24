package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.MadnessCast;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "OTP", collectorNumber = "55")
@CardRegistration(set = "MH2", collectorNumber = "215")
public class TerminalAgony extends Card {

    public TerminalAgony() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new DestroyTargetPermanentEffect());

        addCastingOption(new MadnessCast("{B}{R}"));
    }
}
