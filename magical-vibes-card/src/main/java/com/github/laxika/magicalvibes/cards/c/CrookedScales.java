package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.FlipCoinDestroyTargetOrRepeatEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import java.util.List;

@CardRegistration(set = "MMQ", collectorNumber = "291")
public class CrookedScales extends Card {

    public CrookedScales() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{4}",
                List.of(new FlipCoinDestroyTargetOrRepeatEffect()),
                "{4}, {T}: Flip a coin. If you win the flip, destroy target creature an opponent controls. "
                        + "If you lose the flip, destroy target creature you control unless you pay {3} and repeat this process.",
                List.of(TargetFilters.creatureYouControl(), TargetFilters.creatureAnOpponentControls()),
                2,
                2
        ));
    }
}
