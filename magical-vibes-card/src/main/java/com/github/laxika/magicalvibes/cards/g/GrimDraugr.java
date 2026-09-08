package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;

@CardRegistration(set = "KHM", collectorNumber = "96")
public class GrimDraugr extends Card {

    public GrimDraugr() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{S}",
                List.of(
                        new BoostSelfEffect(1, 0),
                        new GrantKeywordEffect(Keyword.MENACE, GrantScope.SELF)
                ),
                "{1}{S}: This creature gets +1/+0 and gains menace until end of turn."
        ));
    }
}
