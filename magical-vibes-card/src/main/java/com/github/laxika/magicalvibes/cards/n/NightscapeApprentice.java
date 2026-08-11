package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutTargetOnTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "INV", collectorNumber = "112")
public class NightscapeApprentice extends Card {

    public NightscapeApprentice() {
        addActivatedAbility(new ActivatedAbility(true, "{U}",
                List.of(new PutTargetOnTopOfLibraryEffect()),
                "{U}, {T}: Put target creature you control on top of its owner's library.",
                TargetFilters.creatureYouControl()));

        addActivatedAbility(new ActivatedAbility(true, "{R}",
                List.of(new GrantKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.TARGET)),
                "{R}, {T}: Target creature gains first strike until end of turn.",
                TargetFilters.creature()));
    }
}
