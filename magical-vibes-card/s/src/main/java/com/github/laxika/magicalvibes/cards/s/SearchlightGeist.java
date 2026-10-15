package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;

@CardRegistration(set = "AVR", collectorNumber = "119")
public class SearchlightGeist extends Card {

    public SearchlightGeist() {
        // Flying auto-loads from Scryfall.

        // {3}{B}: This creature gains deathtouch until end of turn.
        addActivatedAbility(new ActivatedAbility(false, "{3}{B}",
                List.of(new GrantKeywordEffect(Keyword.DEATHTOUCH, GrantScope.SELF)),
                "{3}{B}: This creature gains deathtouch until end of turn."));
    }
}
