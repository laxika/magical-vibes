package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;

@CardRegistration(set = "ROE", collectorNumber = "68")
public class FrostwindInvoker extends Card {

    public FrostwindInvoker() {
        // {8}: Creatures you control gain flying until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{8}",
                List.of(new GrantKeywordEffect(Keyword.FLYING, GrantScope.ALL_OWN_CREATURES)),
                "{8}: Creatures you control gain flying until end of turn."
        ));
    }
}
