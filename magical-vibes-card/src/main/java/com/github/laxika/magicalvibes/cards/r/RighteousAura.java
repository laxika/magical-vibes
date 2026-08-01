package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.effect.PreventDamageFromChosenSourceEffect;

import java.util.List;

@CardRegistration(set = "VIS", collectorNumber = "20")
public class RighteousAura extends Card {

    public RighteousAura() {
        // {W}, Pay 2 life: The next time a source of your choice would deal damage to you this turn,
        // prevent that damage.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{W}",
                List.of(new PayLifeCost(2), PreventDamageFromChosenSourceEffect.nextDamageToYou()),
                "{W}, Pay 2 life: The next time a source of your choice would deal damage to you this turn, "
                        + "prevent that damage."
        ));
    }
}
