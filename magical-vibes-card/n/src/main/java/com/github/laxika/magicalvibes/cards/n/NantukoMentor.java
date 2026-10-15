package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.TargetPower;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "ODY", collectorNumber = "255")
public class NantukoMentor extends Card {

    public NantukoMentor() {
        // {2}{G}, {T}: Target creature gets +X/+X until end of turn, where X is that creature's power.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}{G}",
                List.of(new BoostTargetCreatureEffect(new TargetPower(), new TargetPower())),
                "{2}{G}, {T}: Target creature gets +X/+X until end of turn, where X is that creature's power.",
                TargetFilters.creature()
        ));
    }
}
