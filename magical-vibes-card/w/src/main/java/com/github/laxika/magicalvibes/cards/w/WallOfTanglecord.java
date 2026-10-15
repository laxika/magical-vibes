package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "SOM", collectorNumber = "222")
public class WallOfTanglecord extends Card {

    public WallOfTanglecord() {
        addActivatedAbility(new ActivatedAbility(false, "{G}", List.of(new GrantKeywordEffect(Keyword.REACH, GrantScope.SELF)), "{G}: Wall of Tanglecord gains reach until end of turn."));
    }
}
