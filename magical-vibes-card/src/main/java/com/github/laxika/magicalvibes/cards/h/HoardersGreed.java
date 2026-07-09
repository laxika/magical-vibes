package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ClashEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;

import java.util.List;

@CardRegistration(set = "LRW", collectorNumber = "117")
public class HoardersGreed extends Card {

    public HoardersGreed() {
        // You lose 2 life and draw two cards, then clash with an opponent. If you win, repeat this
        // process. Each iteration runs the life-loss + draw before clashing; the loop repeats while
        // the controller keeps winning the clash.
        addEffect(EffectSlot.SPELL, new ClashEffect(
                List.of(new LoseLifeEffect(2), new DrawCardEffect(2)), null, true));
    }
}
