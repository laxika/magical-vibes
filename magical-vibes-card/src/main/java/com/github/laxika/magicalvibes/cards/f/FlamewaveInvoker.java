package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerEffect;

import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "202")
public class FlamewaveInvoker extends Card {

    public FlamewaveInvoker() {
        addActivatedAbility(new ActivatedAbility(false, "{7}{R}", List.of(new DealDamageToTargetPlayerEffect(5)), true, "{7}{R}: Flamewave Invoker deals 5 damage to target player or planeswalker."));
    }
}
