package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "SOK", collectorNumber = "131")
public class GhostLitNourisher extends Card {

    public GhostLitNourisher() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}{G}",
                List.of(new BoostTargetCreatureEffect(2, 2)),
                "{2}{G}, {T}: Target creature gets +2/+2 until end of turn.",
                TargetFilters.creature()
        ));

        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{3}{G}",
                List.of(new BoostTargetCreatureEffect(4, 4)),
                "Channel — {3}{G}, Discard this card: Target creature gets +4/+4 until end of turn.",
                TargetFilters.creature()
        ));
    }
}
