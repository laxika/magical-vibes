package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;

@CardRegistration(set = "ELD", collectorNumber = "225")
public class LocthwainGargoyle extends Card {

    public LocthwainGargoyle() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}",
                List.of(
                        new BoostSelfEffect(2, 0),
                        new GrantKeywordEffect(Keyword.FLYING, GrantScope.SELF)
                ),
                "{4}: This creature gets +2/+0 and gains flying until end of turn."
        ));
    }
}
