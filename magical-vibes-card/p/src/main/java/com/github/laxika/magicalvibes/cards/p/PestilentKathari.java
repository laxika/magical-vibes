package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;

@CardRegistration(set = "CON", collectorNumber = "50")
public class PestilentKathari extends Card {

    public PestilentKathari() {
        // {2}{R}: This creature gains first strike until end of turn.
        // Flying and deathtouch are keyword metadata loaded from Scryfall.
        addActivatedAbility(new ActivatedAbility(false, "{2}{R}",
                List.of(new GrantKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.SELF)),
                "{2}{R}: This creature gains first strike until end of turn."));
    }
}
