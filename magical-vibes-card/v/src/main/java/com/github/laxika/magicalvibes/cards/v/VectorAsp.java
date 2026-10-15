package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "SOM", collectorNumber = "219")
public class VectorAsp extends Card {

    public VectorAsp() {
        addActivatedAbility(new ActivatedAbility(false, "{B}", List.of(new GrantKeywordEffect(Keyword.INFECT, GrantScope.SELF)), "{B}: Vector Asp gains infect until end of turn."));
    }
}
