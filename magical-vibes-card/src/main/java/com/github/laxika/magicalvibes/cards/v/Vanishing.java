package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PhaseOutEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "VIS", collectorNumber = "48")
public class Vanishing extends Card {

    public Vanishing() {
        // Enchant creature
        target(TargetFilters.creature());
        // {U}{U}: Enchanted creature phases out.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{U}{U}",
                List.of(new PhaseOutEnchantedCreatureEffect()),
                "{U}{U}: Enchanted creature phases out."
        ));
    }
}
