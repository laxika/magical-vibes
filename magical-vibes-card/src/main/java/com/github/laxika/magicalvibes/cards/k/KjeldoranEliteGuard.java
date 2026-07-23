package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedSacrificeSourceWhenTargetLeavesEffect;

import java.util.List;

@CardRegistration(set = "ICE", collectorNumber = "34")
public class KjeldoranEliteGuard extends Card {

    public KjeldoranEliteGuard() {
        // {T}: Target creature gets +2/+2 until end of turn. When that creature leaves the
        // battlefield this turn, sacrifice this creature. Activate only during combat.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new BoostTargetCreatureEffect(2, 2),
                        new RegisterDelayedSacrificeSourceWhenTargetLeavesEffect()
                ),
                "{T}: Target creature gets +2/+2 until end of turn. When that creature leaves the "
                        + "battlefield this turn, sacrifice this creature. Activate only during combat.",
                null,
                null,
                null,
                ActivationTimingRestriction.ONLY_DURING_COMBAT
        ));
    }
}
