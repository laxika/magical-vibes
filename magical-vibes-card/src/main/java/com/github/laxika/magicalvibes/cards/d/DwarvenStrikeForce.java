package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DiscardRandomCardCost;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ODY", collectorNumber = "188")
public class DwarvenStrikeForce extends Card {

    public DwarvenStrikeForce() {
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new DiscardRandomCardCost(),
                        new GrantKeywordEffect(Set.of(Keyword.FIRST_STRIKE, Keyword.HASTE), GrantScope.SELF)
                ),
                "Discard a card at random: This creature gains first strike and haste until end of turn."
        ));
    }
}
