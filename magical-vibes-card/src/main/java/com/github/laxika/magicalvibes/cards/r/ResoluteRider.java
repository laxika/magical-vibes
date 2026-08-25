package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;

@CardRegistration(set = "ELD", collectorNumber = "214")
public class ResoluteRider extends Card {

    public ResoluteRider() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{W/B}{W/B}",
                List.of(new GrantKeywordEffect(Keyword.LIFELINK, GrantScope.SELF)),
                "{W/B}{W/B}: This creature gains lifelink until end of turn."
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{W/B}{W/B}{W/B}",
                List.of(new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, GrantScope.SELF)),
                "{W/B}{W/B}{W/B}: This creature gains indestructible until end of turn."
        ));
    }
}
