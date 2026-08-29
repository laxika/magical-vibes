package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "MMQ", collectorNumber = "114")
public class WaterfrontBouncer extends Card {

    public WaterfrontBouncer() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{U}",
                List.of(new DiscardCardTypeCost(null, null), ReturnToHandEffect.target()),
                "{U}, {T}, Discard a card: Return target creature to its owner's hand.",
                TargetFilters.creature()
        ));
    }
}
