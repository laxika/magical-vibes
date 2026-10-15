package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;

@CardRegistration(set = "ECL", collectorNumber = "138")
public class FlameChainMauler extends Card {

    public FlameChainMauler() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{R}",
                List.of(
                        new BoostSelfEffect(1, 0),
                        new GrantKeywordEffect(Keyword.MENACE, GrantScope.SELF)
                ),
                "{1}{R}: Flame-Chain Mauler gets +1/+0 and gains menace until end of turn."
        ));
    }
}
