package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "SOK", collectorNumber = "20")
public class MoonwingMoth extends Card {

    public MoonwingMoth() {
        addActivatedAbility(new ActivatedAbility(true, "{W}", List.of(new BoostTargetCreatureEffect(0, 1)),
                "{W}: Target creature gets +0/+1 until end of turn.", TargetFilters.creature()));
    }
}
