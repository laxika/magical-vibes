package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "DRK", collectorNumber = "82")
public class NiallSilvain extends Card {

    public NiallSilvain() {
        // {G}{G}{G}{G}, {T}: Regenerate target creature.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{G}{G}{G}{G}",
                List.of(new RegenerateEffect(true)),
                "{G}{G}{G}{G}, {T}: Regenerate target creature.",
                TargetFilters.creature()
        ));
    }
}
