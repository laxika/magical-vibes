package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "PLC", collectorNumber = "78")
public class RidgedKusite extends Card {

    public RidgedKusite() {
        // {1}{B}, {T}, Discard a card: Target creature gets +1/+0 and gains first strike until end of turn.
        addActivatedAbility(new ActivatedAbility(
                true, "{1}{B}",
                List.of(
                        new DiscardCardTypeCost(null, null),
                        new BoostTargetCreatureEffect(1, 0),
                        new GrantKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.TARGET)
                ),
                "{1}{B}, {T}, Discard a card: Target creature gets +1/+0 and gains first strike until end of turn.",
                TargetFilters.creature()
        ));
    }
}
