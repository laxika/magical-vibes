package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.GrantAdditionalBlockToTargetUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;

@CardRegistration(set = "ODY", collectorNumber = "31")
public class LuminousGuardian extends Card {

    public LuminousGuardian() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{W}",
                List.of(new BoostSelfEffect(0, 1)),
                "{W}: This creature gets +0/+1 until end of turn."
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(new GrantAdditionalBlockToTargetUntilEndOfTurnEffect(1, GrantScope.SELF)),
                "{2}: This creature can block an additional creature this turn."
        ));
    }
}
