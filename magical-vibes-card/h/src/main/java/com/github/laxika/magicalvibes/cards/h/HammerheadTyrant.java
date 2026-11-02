package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetPermanentByCastSpellManaValueEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TDC", collectorNumber = "21")
@CardRegistration(set = "TDC", collectorNumber = "61")
public class HammerheadTyrant extends Card {

    public HammerheadTyrant() {
        target(TargetFilters.nonlandPermanentAnOpponentControls(), 0, 1)
                .addTrigger(Triggers.youCastSpell(),
                        new ReturnTargetPermanentByCastSpellManaValueEffect());
    }
}
