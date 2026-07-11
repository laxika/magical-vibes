package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.ClashEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedManaEqualToTargetSpellManaValueEffect;

@CardRegistration(set = "LRW", collectorNumber = "82")
public class ScatteringStroke extends Card {

    public ScatteringStroke() {
        // Counter target spell. Clash with an opponent. If you win, at the beginning of your next
        // main phase, you may add an amount of {C} equal to that spell's mana value.
        //
        // The clash reward is listed before the counter so that, on a clash win, the targeted spell
        // is still on the stack when the reward snapshots its mana value. The two instructions are
        // independent, so this ordering is rules-equivalent.
        addEffect(EffectSlot.SPELL,
                new ClashEffect(new RegisterDelayedManaEqualToTargetSpellManaValueEffect(ManaColor.COLORLESS)));
        addEffect(EffectSlot.SPELL, new CounterSpellEffect());
    }
}
