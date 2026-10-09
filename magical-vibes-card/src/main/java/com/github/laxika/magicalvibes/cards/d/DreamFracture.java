package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetSpellControllerDrawsCardEffect;

@CardRegistration(set = "EVE", collectorNumber = "19")
@CardRegistration(set = "HBG", collectorNumber = "118")
public class DreamFracture extends Card {

    public DreamFracture() {
        addEffect(EffectSlot.SPELL, new CounterSpellEffect());
        addEffect(EffectSlot.SPELL, new TargetSpellControllerDrawsCardEffect());
        addEffect(EffectSlot.SPELL, new DrawCardEffect());
    }
}
