package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;

@CardRegistration(set = "FEM", collectorNumber = "7a")
@CardRegistration(set = "FEM", collectorNumber = "7b")
@CardRegistration(set = "FEM", collectorNumber = "7c")
@CardRegistration(set = "FEM", collectorNumber = "7d")
@CardRegistration(set = "FEM", collectorNumber = "145")
@CardRegistration(set = "FEM", collectorNumber = "146")
public class IcatianInfantry extends Card {

    public IcatianInfantry() {
        addActivatedAbility(new ActivatedAbility(false, "{1}",
                List.of(new GrantKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.SELF)),
                "{1}: This creature gains first strike until end of turn."));
        addActivatedAbility(new ActivatedAbility(false, "{1}",
                List.of(new GrantKeywordEffect(Keyword.BANDING, GrantScope.SELF)),
                "{1}: This creature gains banding until end of turn."));
    }
}
