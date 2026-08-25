package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PlayersCantSearchLibrariesThisTurnEffect;

@CardRegistration(set = "RAV", collectorNumber = "253")
public class ShadowOfDoubt extends Card {

    public ShadowOfDoubt() {
        addEffect(EffectSlot.SPELL, new PlayersCantSearchLibrariesThisTurnEffect());
        addEffect(EffectSlot.SPELL, new DrawCardEffect(1));
    }
}
