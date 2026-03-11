package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.DealDividedDamageToAnyTargetsEffect;

import java.util.List;

@CardRegistration(set = "M11", collectorNumber = "146")
public class InfernoTitan extends Card {

    public InfernoTitan() {
        // {R}: Inferno Titan gets +1/+0 until end of turn.
        addActivatedAbility(new ActivatedAbility(false, "{R}", List.of(new BoostSelfEffect(1, 0)),
                "{R}: Inferno Titan gets +1/+0 until end of turn."));

        // Whenever Inferno Titan enters the battlefield or attacks,
        // it deals 3 damage divided as you choose among one, two, or three targets.
        DealDividedDamageToAnyTargetsEffect dividedDamage = new DealDividedDamageToAnyTargetsEffect(3, 3);
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, dividedDamage);
        addEffect(EffectSlot.ON_ATTACK, dividedDamage);
    }
}
