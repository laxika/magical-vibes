package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;

import com.github.laxika.magicalvibes.model.effect.Subjects;
import java.util.List;

@CardRegistration(set = "CON", collectorNumber = "45")
public class Fleshformer extends Card {

    public Fleshformer() {
        // {W}{U}{B}{R}{G}: This creature gets +2/+2 and gains fear until end of turn.
        // Target creature gets -2/-2 until end of turn. Activate only during your turn.
        addActivatedAbility(new ActivatedAbility(
                false, "{W}{U}{B}{R}{G}",
                List.of(
                        BoostEffect.self(2, 2),
                        new GrantKeywordEffect(Keyword.FEAR, Subjects.source()),
                        BoostEffect.target(-2, -2)
                ),
                "{W}{U}{B}{R}{G}: This creature gets +2/+2 and gains fear until end of turn. "
                        + "Target creature gets -2/-2 until end of turn. Activate only during your turn.",
                ActivationTimingRestriction.ONLY_DURING_YOUR_TURN
        ));
    }
}
