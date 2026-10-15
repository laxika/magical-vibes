package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.amount.TargetPower;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

public class WingedTempleOfOrazca extends Card {

    public WingedTempleOfOrazca() {
        addActivatedAbility(ManaAbilities.tapForAnyColor());

        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{G}{U}",
                List.of(
                        new GrantKeywordEffect(Keyword.FLYING, GrantScope.TARGET),
                        new BoostTargetCreatureEffect(new TargetPower(), new TargetPower())
                ),
                "{1}{G}{U}, {T}: Target creature you control gains flying and gets +X/+X until end of turn, where X is its power.",
                TargetFilters.creatureYouControl()
        ));
    }
}
