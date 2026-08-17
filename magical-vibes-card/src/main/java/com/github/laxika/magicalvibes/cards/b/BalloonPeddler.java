package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "MMQ", collectorNumber = "59")
public class BalloonPeddler extends Card {

    public BalloonPeddler() {
        // {U}, {T}, Discard a card: Target creature gains flying until end of turn.
        addActivatedAbility(new ActivatedAbility(
                true, "{U}",
                List.of(
                        new DiscardCardTypeCost(null, null),
                        new GrantKeywordEffect(Keyword.FLYING, GrantScope.TARGET)
                ),
                "{U}, {T}, Discard a card: Target creature gains flying until end of turn.",
                TargetFilters.creature()
        ));
    }
}
