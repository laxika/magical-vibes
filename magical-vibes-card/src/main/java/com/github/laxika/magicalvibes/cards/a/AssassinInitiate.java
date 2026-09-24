package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantChosenKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;

@CardRegistration(set = "ACR", collectorNumber = "22")
public class AssassinInitiate extends Card {

    public AssassinInitiate() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(new GrantChosenKeywordEffect(
                        List.of(Keyword.FLYING, Keyword.DEATHTOUCH, Keyword.LIFELINK), GrantScope.SELF)),
                "{1}: This creature gains your choice of flying, deathtouch, or lifelink until end of turn."
        ));
    }
}
