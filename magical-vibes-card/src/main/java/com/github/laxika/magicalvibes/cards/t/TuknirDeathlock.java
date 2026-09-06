package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "LEG", collectorNumber = "267")
public class TuknirDeathlock extends Card {

    public TuknirDeathlock() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{R}{G}",
                List.of(new BoostTargetCreatureEffect(2, 2)),
                "{R}{G}, {T}: Target creature gets +2/+2 until end of turn.",
                TargetFilters.creature()
        ));
    }
}
