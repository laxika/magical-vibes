package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;

import java.util.List;

@CardRegistration(set = "XLN", collectorNumber = "12")
public class GlorifierOfDusk extends Card {

    public GlorifierOfDusk() {
        // Pay 2 life: Glorifier of Dusk gains flying until end of turn.
        addActivatedAbility(new ActivatedAbility(false, null,
                List.of(new PayLifeCost(2),
                        new GrantKeywordEffect(Keyword.FLYING, GrantScope.SELF)),
                "Pay 2 life: Glorifier of Dusk gains flying until end of turn."));

        // Pay 2 life: Glorifier of Dusk gains vigilance until end of turn.
        addActivatedAbility(new ActivatedAbility(false, null,
                List.of(new PayLifeCost(2),
                        new GrantKeywordEffect(Keyword.VIGILANCE, GrantScope.SELF)),
                "Pay 2 life: Glorifier of Dusk gains vigilance until end of turn."));
    }
}
