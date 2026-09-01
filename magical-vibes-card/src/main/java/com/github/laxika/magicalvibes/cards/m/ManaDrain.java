package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedManaEqualToTargetSpellManaValueEffect;

@CardRegistration(set = "LEG", collectorNumber = "65")
public class ManaDrain extends Card {

    public ManaDrain() {
        addEffect(EffectSlot.SPELL, new RegisterDelayedManaEqualToTargetSpellManaValueEffect(
                ManaColor.COLORLESS, false, false, false));
        addEffect(EffectSlot.SPELL, new CounterSpellEffect());
    }
}
