package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;

@CardRegistration(set = "OGW", collectorNumber = "158")
public class RelentlessHunter extends Card {

    public RelentlessHunter() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{R}{G}",
                List.of(
                        new BoostSelfEffect(1, 1),
                        new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.SELF)
                ),
                "{1}{R}{G}: This creature gets +1/+1 and gains trample until end of turn."
        ));
    }
}
