package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;

@CardRegistration(set = "HBG", collectorNumber = "104")
public class SteadfastUnicorn extends Card {

    public SteadfastUnicorn() {
        // {3}{W}: Creatures you control get +1/+1 and gain vigilance until end of turn.
        // Activate only during your turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{W}",
                List.of(
                        new BoostAllOwnCreaturesEffect(1, 1),
                        new GrantKeywordEffect(Keyword.VIGILANCE, GrantScope.ALL_OWN_CREATURES)
                ),
                "{3}{W}: Creatures you control get +1/+1 and gain vigilance until end of turn. "
                        + "Activate only during your turn.",
                ActivationTimingRestriction.ONLY_DURING_YOUR_TURN
        ));
    }
}
