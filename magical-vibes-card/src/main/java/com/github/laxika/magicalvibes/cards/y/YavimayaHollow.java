package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "UDS", collectorNumber = "143")
public class YavimayaHollow extends Card {

    public YavimayaHollow() {
        // {T}: Add {C}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));
        // {G}, {T}: Regenerate target creature.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{G}",
                List.of(new RegenerateEffect(true)),
                "{G}, {T}: Regenerate target creature.",
                TargetFilters.creature()
        ));
    }
}
