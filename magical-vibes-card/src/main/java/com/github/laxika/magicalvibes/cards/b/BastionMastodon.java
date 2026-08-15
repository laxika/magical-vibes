package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;

@CardRegistration(set = "KLD", collectorNumber = "197")
public class BastionMastodon extends Card {

    public BastionMastodon() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{W}",
                List.of(new GrantKeywordEffect(Keyword.VIGILANCE, GrantScope.SELF)),
                "{W}: Bastion Mastodon gains vigilance until end of turn."
        ));
    }
}
