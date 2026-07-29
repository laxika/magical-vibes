package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RemoveKeywordEffect;

import java.util.List;

@CardRegistration(set = "MIR", collectorNumber = "271")
public class LeeringGargoyle extends Card {

    public LeeringGargoyle() {
        // {T}: This creature gets -2/+2 and loses flying until end of turn.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new BoostSelfEffect(-2, 2),
                        new RemoveKeywordEffect(Keyword.FLYING, GrantScope.SELF)),
                "{T}: Leering Gargoyle gets -2/+2 and loses flying until end of turn."
        ));
    }
}
