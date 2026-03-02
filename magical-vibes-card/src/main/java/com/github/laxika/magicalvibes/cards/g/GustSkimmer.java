package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "MBS", collectorNumber = "108")
public class GustSkimmer extends Card {

    public GustSkimmer() {
        addActivatedAbility(new ActivatedAbility(false, "{U}", List.of(new GrantKeywordEffect(Keyword.FLYING, GrantScope.SELF)), "{U}: Gust-Skimmer gains flying until end of turn."));
    }
}
