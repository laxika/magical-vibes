package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSelfToHandCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "TMP", collectorNumber = "216")
public class BrokenFall extends Card {

    public BrokenFall() {
        // Return this enchantment to its owner's hand: Regenerate target creature.
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new ReturnSelfToHandCost(), new RegenerateEffect(true)),
                "Return this enchantment to its owner's hand: Regenerate target creature.",
                TargetFilters.creature()
        ));
    }
}
