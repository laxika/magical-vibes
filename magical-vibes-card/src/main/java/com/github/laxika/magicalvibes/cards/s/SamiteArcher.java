package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;

import java.util.List;

@CardRegistration(set = "INV", collectorNumber = "269")
public class SamiteArcher extends Card {

    public SamiteArcher() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(PreventDamageEffect.nextToTarget(1)),
                "{T}: Prevent the next 1 damage that would be dealt to any target this turn."
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new DealDamageToAnyTargetEffect(1)),
                "{T}: This creature deals 1 damage to any target."
        ));
    }
}
