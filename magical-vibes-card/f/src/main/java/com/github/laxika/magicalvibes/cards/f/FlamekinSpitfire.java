package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;

import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;

@CardRegistration(set = "LRW", collectorNumber = "168")
public class FlamekinSpitfire extends Card {

    public FlamekinSpitfire() {
        addActivatedAbility(new ActivatedAbility(false, "{3}{R}", List.of(DealDamageEffect.toAnyTarget(1)), "{3}{R}: Flamekin Spitfire deals 1 damage to any target."));
    }
}
