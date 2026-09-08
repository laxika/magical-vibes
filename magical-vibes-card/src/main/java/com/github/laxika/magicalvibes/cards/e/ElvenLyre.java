package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "FEM", collectorNumber = "87")
@CardRegistration(set = "FEM", collectorNumber = "172")
public class ElvenLyre extends Card {

    public ElvenLyre() {
        // {1}, {T}, Sacrifice this artifact: Target creature gets +2/+2 until end of turn.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new SacrificeSelfCost(), new BoostTargetCreatureEffect(2, 2)),
                "{1}, {T}, Sacrifice Elven Lyre: Target creature gets +2/+2 until end of turn.",
                TargetFilters.creature()
        ));
    }
}
