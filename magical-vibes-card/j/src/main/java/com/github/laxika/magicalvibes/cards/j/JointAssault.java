package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostPairedCreatureOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "AVR", collectorNumber = "183")
public class JointAssault extends Card {

    public JointAssault() {
        // Target creature gets +2/+2 until end of turn. If it's paired with a creature, that
        // creature also gets +2/+2 until end of turn.
        target(TargetFilters.creature())
                .addSpellEffect(new BoostTargetCreatureEffect(2, 2))
                .addSpellEffect(new BoostPairedCreatureOfTargetEffect(2, 2));
    }
}
