package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;

@CardRegistration(set = "KLD", collectorNumber = "130")
public class SkyshipStalker extends Card {

    public SkyshipStalker() {
        addActivatedAbility(new ActivatedAbility(false, "{R}", List.of(new BoostSelfEffect(1, 0)),
                "{R}: This creature gets +1/+0 until end of turn."));
        addActivatedAbility(new ActivatedAbility(false, "{R}", List.of(new GrantKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.SELF)),
                "{R}: This creature gains first strike until end of turn."));
        addActivatedAbility(new ActivatedAbility(false, "{R}", List.of(new GrantKeywordEffect(Keyword.HASTE, GrantScope.SELF)),
                "{R}: This creature gains haste until end of turn."));
    }
}
