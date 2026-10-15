package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "M19", collectorNumber = "136")
public class DismissivePyromancer extends Card {

    public DismissivePyromancer() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{R}",
                List.of(new DiscardCardTypeCost(null, null), new DrawCardEffect()),
                "{R}, {T}, Discard a card: Draw a card."
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}{R}",
                List.of(new SacrificeSelfCost(), new DealDamageToTargetCreatureEffect(4)),
                "{2}{R}, {T}, Sacrifice this creature: It deals 4 damage to target creature.",
                TargetFilters.creature()
        ));
    }
}
