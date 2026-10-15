package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

import java.util.List;

@CardRegistration(set = "M20", collectorNumber = "236")
public class RetributiveWand extends Card {

    public RetributiveWand() {
        // {3}, {T}: This artifact deals 1 damage to any target.
        addActivatedAbility(new ActivatedAbility(true, "{3}", List.of(new DealDamageToAnyTargetEffect(1)),
                "{3}, {T}: This artifact deals 1 damage to any target."));

        // When this artifact is put into a graveyard from the battlefield, it deals 5 damage to any target.
        addEffect(EffectSlot.ON_SELF_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD,
                new DealDamageToAnyTargetEffect(5));
    }
}
