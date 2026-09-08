package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOpponentGainsControlOfSourceEffect;
import com.github.laxika.magicalvibes.model.effect.DelayedEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.effect.FlipCoinWinEffect;

@CardRegistration(set = "PCY", collectorNumber = "89")
public class FickleEfreet extends Card {

    public FickleEfreet() {
        DelayedEndOfCombatEffect flip = new DelayedEndOfCombatEffect(
                new FlipCoinWinEffect(null, new ChooseOpponentGainsControlOfSourceEffect()));
        addEffect(EffectSlot.ON_ATTACK, flip);
        addEffect(EffectSlot.ON_BLOCK, flip);
    }
}
