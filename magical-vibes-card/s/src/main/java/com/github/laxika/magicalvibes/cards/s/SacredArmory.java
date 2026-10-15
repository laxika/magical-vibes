package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "M15", collectorNumber = "228")
public class SacredArmory extends Card {

    public SacredArmory() {
        addActivatedAbility(new ActivatedAbility(
                false, "{2}",
                List.of(new BoostTargetCreatureEffect(1, 0)),
                "{2}: Target creature gets +1/+0 until end of turn.",
                TargetFilters.creature()
        ));
    }
}
