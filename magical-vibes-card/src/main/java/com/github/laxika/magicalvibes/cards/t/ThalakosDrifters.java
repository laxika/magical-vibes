package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;

@CardRegistration(set = "EXO", collectorNumber = "47")
@CardRegistration(set = "TPR", collectorNumber = "70")
public class ThalakosDrifters extends Card {

    public ThalakosDrifters() {
        // Discard a card: This creature gains shadow until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new DiscardCardTypeCost(null, null),
                        new GrantKeywordEffect(Keyword.SHADOW, GrantScope.SELF)
                ),
                "Discard a card: Thalakos Drifters gains shadow until end of turn."
        ));
    }
}
