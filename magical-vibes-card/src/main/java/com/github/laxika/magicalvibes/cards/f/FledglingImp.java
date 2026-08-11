package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;

@CardRegistration(set = "ODY", collectorNumber = "137")
public class FledglingImp extends Card {

    public FledglingImp() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{B}",
                List.of(new DiscardCardTypeCost(null, null), new GrantKeywordEffect(Keyword.FLYING, GrantScope.SELF)),
                "{B}, Discard a card: This creature gains flying until end of turn."));
    }
}
