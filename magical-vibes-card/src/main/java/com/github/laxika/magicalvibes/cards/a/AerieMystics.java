package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;

@CardRegistration(set = "CON", collectorNumber = "1")
public class AerieMystics extends Card {

    public AerieMystics() {
        // {1}{G}{U}: Creatures you control gain shroud until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{G}{U}",
                List.of(new GrantKeywordEffect(Keyword.SHROUD, GrantScope.OWN_CREATURES)),
                "{1}{G}{U}: Creatures you control gain shroud until end of turn."
        ));
    }
}
