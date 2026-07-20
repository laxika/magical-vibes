package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BountyOfTheLuxaEffect;

@CardRegistration(set = "AKH", collectorNumber = "196")
public class BountyOfTheLuxa extends Card {

    public BountyOfTheLuxa() {
        // At the beginning of your first main phase, remove all flood counters from this
        // enchantment. If no counters were removed this way, put a flood counter on this
        // enchantment and draw a card. Otherwise, add {C}{G}{U}.
        addEffect(EffectSlot.PRECOMBAT_MAIN_TRIGGERED, new BountyOfTheLuxaEffect());
    }
}
