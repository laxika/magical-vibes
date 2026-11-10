package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DisturbCast;
import com.github.laxika.magicalvibes.model.effect.SacrificeAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "VOW", collectorNumber = "235")
@CardRegistration(set = "DBL", collectorNumber = "502")
public class DorotheaVengefulVictim extends Card {

    public DorotheaVengefulVictim() {
        setBackFaceCard(new DorotheasRetribution());

        addTrigger(List.of(Triggers.attacks(), Triggers.blocks()), TriggerOptions.ONE_OR_MORE,
                new SacrificeAtEndOfCombatEffect());
        addCastingOption(new DisturbCast("{1}{W}{U}"));
    }

    @Override
    public String getBackFaceClassName() {
        return "DorotheasRetribution";
    }
}
