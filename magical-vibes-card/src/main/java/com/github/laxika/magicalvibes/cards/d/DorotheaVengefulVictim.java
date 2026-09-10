package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DisturbCast;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SacrificeAtEndOfCombatEffect;

@CardRegistration(set = "VOW", collectorNumber = "235")
public class DorotheaVengefulVictim extends Card {

    public DorotheaVengefulVictim() {
        setBackFaceCard(new DorotheasRetribution());

        addEffect(EffectSlot.ON_ATTACK, new SacrificeAtEndOfCombatEffect());
        addEffect(EffectSlot.ON_BLOCK, new SacrificeAtEndOfCombatEffect());
        addCastingOption(new DisturbCast("{1}{W}{U}"));
    }

    @Override
    public String getBackFaceClassName() {
        return "DorotheasRetribution";
    }
}
