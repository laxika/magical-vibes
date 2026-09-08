package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.SnowManaSpentToCast;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;

@CardRegistration(set = "KHM", collectorNumber = "61")
public class GravenLore extends Card {

    public GravenLore() {
        addEffect(EffectSlot.SPELL, new ScryEffect(new SnowManaSpentToCast()));
        addEffect(EffectSlot.SPELL, new DrawCardEffect(3));
    }
}
