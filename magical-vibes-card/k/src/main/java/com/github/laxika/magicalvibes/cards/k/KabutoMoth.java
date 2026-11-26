package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "CHK", collectorNumber = "20")
public class KabutoMoth extends Card {

    public KabutoMoth() {
        addActivatedAbility(new ActivatedAbility(true, null, List.of(BoostEffect.target(1, 2)),
                "{T}: Target creature gets +1/+2 until end of turn.", TargetFilters.creature()));
    }
}
