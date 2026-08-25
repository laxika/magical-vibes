package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRandomCardCost;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;

@CardRegistration(set = "TOR", collectorNumber = "107")
public class PardicLancer extends Card {

    public PardicLancer() {
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new DiscardRandomCardCost(),
                        new BoostSelfEffect(1, 0),
                        new GrantKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.SELF)
                ),
                "Discard a card at random: This creature gets +1/+0 and gains first strike until end of turn."
        ));
    }
}
