package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardRandomCardCost;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "FEM", collectorNumber = "86")
public class DraconianCylix extends Card {

    public DraconianCylix() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(new DiscardRandomCardCost(), new RegenerateEffect(true)),
                "{2}, {T}, Discard a card at random: Regenerate target creature.",
                TargetFilters.creature()
        ));
    }
}
