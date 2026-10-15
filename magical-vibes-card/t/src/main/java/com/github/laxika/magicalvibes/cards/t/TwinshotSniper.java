package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

import java.util.List;

@CardRegistration(set = "NEO", collectorNumber = "168")
public class TwinshotSniper extends Card {

    public TwinshotSniper() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DealDamageToAnyTargetEffect(2));

        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{1}{R}",
                List.of(new DealDamageToAnyTargetEffect(2)),
                "Channel — {1}{R}, Discard this card: It deals 2 damage to any target."
        ));
    }
}
