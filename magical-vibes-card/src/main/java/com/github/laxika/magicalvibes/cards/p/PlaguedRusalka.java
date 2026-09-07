package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "GPT", collectorNumber = "56")
public class PlaguedRusalka extends Card {

    public PlaguedRusalka() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{B}",
                List.of(new SacrificeCreatureCost(), new BoostTargetCreatureEffect(-1, -1)),
                "{B}, Sacrifice a creature: Target creature gets -1/-1 until end of turn.",
                TargetFilters.creature()
        ));
    }
}
