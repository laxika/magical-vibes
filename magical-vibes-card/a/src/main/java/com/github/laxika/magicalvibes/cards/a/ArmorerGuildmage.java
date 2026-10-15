package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "MIR", collectorNumber = "156")
public class ArmorerGuildmage extends Card {

    public ArmorerGuildmage() {
        addActivatedAbility(new ActivatedAbility(true, "{B}",
                List.of(new BoostTargetCreatureEffect(1, 0)),
                "{B}, {T}: Target creature gets +1/+0 until end of turn.",
                TargetFilters.creature()));

        addActivatedAbility(new ActivatedAbility(true, "{G}",
                List.of(new BoostTargetCreatureEffect(0, 1)),
                "{G}, {T}: Target creature gets +0/+1 until end of turn.",
                TargetFilters.creature()));
    }
}
