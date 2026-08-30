package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;

@CardRegistration(set = "MIR", collectorNumber = "280")
@CardRegistration(set = "GPT", collectorNumber = "127")
public class SavageTwister extends Card {

    public SavageTwister() {
        addEffect(EffectSlot.SPELL, new MassDamageEffect(0, true, false, null));
    }
}
