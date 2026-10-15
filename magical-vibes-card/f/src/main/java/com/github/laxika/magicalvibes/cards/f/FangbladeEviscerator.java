package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;

public class FangbladeEviscerator extends Card {

    public FangbladeEviscerator() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{R}",
                List.of(
                        new BoostSelfEffect(1, 0),
                        new GrantKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.SELF)
                ),
                "{1}{R}: This creature gets +1/+0 and gains first strike until end of turn."
        ));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}{R}",
                List.of(new BoostAllOwnCreaturesEffect(2, 0)),
                "{4}{R}: Creatures you control get +2/+0 until end of turn."
        ));
    }
}
