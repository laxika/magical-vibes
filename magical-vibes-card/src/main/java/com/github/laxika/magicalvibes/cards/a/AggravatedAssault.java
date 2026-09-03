package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AdditionalCombatMainPhaseEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "ONS", collectorNumber = "185")
public class AggravatedAssault extends Card {

    public AggravatedAssault() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{R}{R}",
                List.of(
                        new UntapPermanentsEffect(TapUntapScope.CONTROLLED, new PermanentIsCreaturePredicate()),
                        new AdditionalCombatMainPhaseEffect(1)
                ),
                "{3}{R}{R}: Untap all creatures you control. After this main phase, there is an additional combat phase followed by an additional main phase. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
