package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;
import com.github.laxika.magicalvibes.model.effect.ExpropriateEffect;

@CardRegistration(set = "SPG", collectorNumber = "66")
public class Expropriate extends Card {

    public Expropriate() {
        addEffect(EffectSlot.SPELL, new ExpropriateEffect());
        addEffect(EffectSlot.SPELL, new ExileSpellEffect());
    }
}
