package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterSpellAndCreateTreasureTokensEffect;

@CardRegistration(set = "XLN", collectorNumber = "82")
public class SpellSwindle extends Card {

    public SpellSwindle() {
        addEffect(EffectSlot.SPELL, new CounterSpellAndCreateTreasureTokensEffect());
    }
}
