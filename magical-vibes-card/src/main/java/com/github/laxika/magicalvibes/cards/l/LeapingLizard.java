package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;

@CardRegistration(set = "HML", collectorNumber = "90")
public class LeapingLizard extends Card {

    public LeapingLizard() {
        // {1}{G}: This creature gets -0/-1 and gains flying until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{G}",
                List.of(new BoostSelfEffect(0, -1),
                        new GrantKeywordEffect(Keyword.FLYING, GrantScope.SELF)),
                "{1}{G}: Leaping Lizard gets -0/-1 and gains flying until end of turn."
        ));
    }
}
