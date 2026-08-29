package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ForetellCast;
import com.github.laxika.magicalvibes.model.effect.CopyNextInstantOrSorceryCastThisTurnEffect;

@CardRegistration(set = "KHM", collectorNumber = "132")
public class DualStrike extends Card {

    public DualStrike() {
        addEffect(EffectSlot.SPELL, new CopyNextInstantOrSorceryCastThisTurnEffect(4));
        addCastingOption(new ForetellCast("{R}"));
    }
}
