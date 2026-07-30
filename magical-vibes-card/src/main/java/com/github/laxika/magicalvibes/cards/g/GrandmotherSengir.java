package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "HML", collectorNumber = "50")
public class GrandmotherSengir extends Card {

    public GrandmotherSengir() {
        addActivatedAbility(new ActivatedAbility(true, "{1}{B}",
                List.of(new BoostTargetCreatureEffect(-1, -1)),
                "{1}{B}, {T}: Target creature gets -1/-1 until end of turn.",
                TargetFilters.creature()));
    }
}
