package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToSourceEffect;

import java.util.List;

@CardRegistration(set = "4ED", collectorNumber = "95")
public class PsionicEntity extends Card {

    public PsionicEntity() {
        // {T}: This creature deals 2 damage to any target and 3 damage to itself.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new DealDamageToAnyTargetEffect(2), new DealDamageToSourceEffect(3)),
                "{T}: Psionic Entity deals 2 damage to any target and 3 damage to itself."
        ));
    }
}
