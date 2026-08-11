package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.TargetSpellControllerLosesLifeEffect;

@CardRegistration(set = "INV", collectorNumber = "282")
public class Undermine extends Card {

    public Undermine() {
        addEffect(EffectSlot.SPELL, new TargetSpellControllerLosesLifeEffect(3));
        addEffect(EffectSlot.SPELL, new CounterSpellEffect());
    }
}
