package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.ClashEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedManaEqualToTargetSpellManaValueEffect;

@CardRegistration(set = "LRW", collectorNumber = "82")
@CardRegistration(set = "CMD", collectorNumber = "60")
public class ScatteringStroke extends Card {

    public ScatteringStroke() {
        // Counter target spell. Clash with an opponent. If you win, at the beginning of your next
        // main phase, you may add an amount of {C} equal to that spell's mana value.
        addSpellEffect(new CounterSpellEffect());
        addSpellEffect(new ClashEffect(new RegisterDelayedManaEqualToTargetSpellManaValueEffect(ManaColor.COLORLESS)));
    }
}
