package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.FlipCoinDoubleOrPreventNextDamageFromChosenSourceEffect;

@CardRegistration(set = "WTH", collectorNumber = "96")
public class DesperateGambit extends Card {

    public DesperateGambit() {
        // Choose a source you control and flip a coin. If you win the flip, the next time that source
        // would deal damage this turn, it deals double that damage instead. If you lose the flip, the
        // next time it would deal damage this turn, prevent that damage.
        addEffect(EffectSlot.SPELL, new FlipCoinDoubleOrPreventNextDamageFromChosenSourceEffect());
    }
}
