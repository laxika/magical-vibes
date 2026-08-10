package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "MRD", collectorNumber = "220")
public class NimReplica extends Card {

    public NimReplica() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{B}",
                List.of(new SacrificeSelfCost(), new BoostTargetCreatureEffect(-1, -1)),
                "{2}{B}, Sacrifice Nim Replica: Target creature gets -1/-1 until end of turn.",
                TargetFilters.creature()
        ));
    }
}
