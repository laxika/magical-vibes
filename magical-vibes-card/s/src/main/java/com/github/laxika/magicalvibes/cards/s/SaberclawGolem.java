package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;

@CardRegistration(set = "SOM", collectorNumber = "200")
public class SaberclawGolem extends Card {

    public SaberclawGolem() {
        addActivatedAbility(new ActivatedAbility(false, "{R}", List.of(new GrantKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.SELF)), "{R}: Saberclaw Golem gains first strike until end of turn."));
    }
}
