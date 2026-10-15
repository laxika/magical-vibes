package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;

@CardRegistration(set = "DGM", collectorNumber = "54")
public class BeetleformMage extends Card {

    public BeetleformMage() {
        // {G}{U}: Beetleform Mage gets +2/+2 and gains flying until end of turn. Activate only once each turn.
        addActivatedAbility(new ActivatedAbility(false, "{G}{U}",
                List.of(new BoostSelfEffect(2, 2),
                        new GrantKeywordEffect(Keyword.FLYING, GrantScope.SELF)),
                "{G}{U}: Beetleform Mage gets +2/+2 and gains flying until end of turn. Activate only once each turn.",
                1));
    }
}
