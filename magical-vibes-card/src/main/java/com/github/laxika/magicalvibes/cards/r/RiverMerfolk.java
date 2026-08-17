package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;

@CardRegistration(set = "FEM", collectorNumber = "24")
@CardRegistration(set = "FEM", collectorNumber = "144")
public class RiverMerfolk extends Card {

    public RiverMerfolk() {
        addActivatedAbility(new ActivatedAbility(false, "{U}",
                List.of(new GrantKeywordEffect(Keyword.MOUNTAINWALK, GrantScope.SELF)),
                "{U}: This creature gains mountainwalk until end of turn."));
    }
}
