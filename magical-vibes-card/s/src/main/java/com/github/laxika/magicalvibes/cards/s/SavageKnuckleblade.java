package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;

import java.util.List;

@CardRegistration(set = "KTK", collectorNumber = "197")
public class SavageKnuckleblade extends Card {

    public SavageKnuckleblade() {
        // {2}{G}: This creature gets +2/+2 until end of turn. Activate only once each turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{G}",
                List.of(new BoostSelfEffect(2, 2)),
                "{2}{G}: This creature gets +2/+2 until end of turn. Activate only once each turn.",
                1
        ));

        // {2}{U}: Return this creature to its owner's hand.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{U}",
                List.of(ReturnToHandEffect.self()),
                "{2}{U}: Return this creature to its owner's hand."
        ));

        // {R}: This creature gains haste until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{R}",
                List.of(new GrantKeywordEffect(Keyword.HASTE, GrantScope.SELF)),
                "{R}: This creature gains haste until end of turn."
        ));
    }
}
