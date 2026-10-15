package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "NEM", collectorNumber = "82")
public class FlowstoneOverseer extends Card {

    public FlowstoneOverseer() {
        addActivatedAbility(new ActivatedAbility(false, "{R}{R}",
                List.of(new BoostTargetCreatureEffect(1, -1)),
                "{R}{R}: Target creature gets +1/-1 until end of turn.",
                TargetFilters.creature()));
    }
}
