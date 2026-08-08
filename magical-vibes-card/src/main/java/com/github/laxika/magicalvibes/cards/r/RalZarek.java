package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ControllerExtraTurnEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.FlipCoinsPerHeadsEffect;
import com.github.laxika.magicalvibes.model.effect.TapTargetThenUntapAnotherTargetEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "DGM", collectorNumber = "94")
public class RalZarek extends Card {

    public RalZarek() {
        // +1: Tap target permanent, then untap another target permanent.
        addActivatedAbility(new ActivatedAbility(
                false, null,
                List.of(new TapTargetThenUntapAnotherTargetEffect()),
                "+1: Tap target permanent, then untap another target permanent.",
                null, +1, null, null,
                List.of(
                        TargetFilters.permanent(),
                        TargetFilters.permanent()
                ),
                2, 2
        ));

        // −2: Ral Zarek deals 3 damage to any target.
        addActivatedAbility(new ActivatedAbility(
                -2,
                List.of(new DealDamageToAnyTargetEffect(3)),
                "−2: Ral Zarek deals 3 damage to any target."
        ));

        // −7: Flip five coins. Take an extra turn after this one for each coin that comes up heads.
        addActivatedAbility(new ActivatedAbility(
                -7,
                List.of(new FlipCoinsPerHeadsEffect(5, new ControllerExtraTurnEffect(1))),
                "−7: Flip five coins. Take an extra turn after this one for each coin that comes up heads."
        ));
    }
}
