package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.TargetSpellControllerLooksAtHandEffect;

@CardRegistration(set = "ROE", collectorNumber = "74")
public class LayBare extends Card {

    public LayBare() {
        addEffect(EffectSlot.SPELL, new TargetSpellControllerLooksAtHandEffect());
        addEffect(EffectSlot.SPELL, new CounterSpellEffect());
    }
}
