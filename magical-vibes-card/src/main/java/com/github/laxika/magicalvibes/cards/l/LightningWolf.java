package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;

@CardRegistration(set = "VOW", collectorNumber = "168")
public class LightningWolf extends Card {

    public LightningWolf() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{R}",
                List.of(new GrantKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.SELF)),
                "{1}{R}: This creature gains first strike until end of turn.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
