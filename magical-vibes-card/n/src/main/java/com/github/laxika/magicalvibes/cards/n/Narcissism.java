package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "TOR", collectorNumber = "134")
public class Narcissism extends Card {

    public Narcissism() {
        // {G}, Discard a card: Target creature gets +2/+2 until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{G}",
                List.of(new DiscardCardTypeCost(null, null), new BoostTargetCreatureEffect(2, 2)),
                "{G}, Discard a card: Target creature gets +2/+2 until end of turn.",
                TargetFilters.creature()
        ));

        // {G}, Sacrifice this enchantment: Target creature gets +2/+2 until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{G}",
                List.of(new SacrificeSelfCost(), new BoostTargetCreatureEffect(2, 2)),
                "{G}, Sacrifice this enchantment: Target creature gets +2/+2 until end of turn.",
                TargetFilters.creature()
        ));
    }
}
