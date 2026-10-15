package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "ODY", collectorNumber = "279")
public class Twigwalker extends Card {

    public Twigwalker() {
        // {1}{G}, Sacrifice this creature: Two target creatures each get +2/+2 until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{G}",
                List.of(new SacrificeSelfCost(), new BoostTargetCreatureEffect(2, 2)),
                "{1}{G}, Sacrifice Twigwalker: Two target creatures each get +2/+2 until end of turn.",
                List.of(TargetFilters.creature(), TargetFilters.creature()),
                2,
                2
        ));
    }
}
