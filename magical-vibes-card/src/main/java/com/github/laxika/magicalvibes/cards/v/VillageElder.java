package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "MIR", collectorNumber = "251")
@CardRegistration(set = "BRB", collectorNumber = "94")
public class VillageElder extends Card {

    public VillageElder() {
        // {G}, {T}, Sacrifice a Forest: Regenerate target creature.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{G}",
                List.of(
                        new SacrificePermanentCost(
                                new PermanentHasSubtypePredicate(CardSubtype.FOREST),
                                "Sacrifice a Forest",
                                false
                        ),
                        new RegenerateEffect(true)
                ),
                "{G}, {T}, Sacrifice a Forest: Regenerate target creature.",
                TargetFilters.creature()
        ));
    }
}
