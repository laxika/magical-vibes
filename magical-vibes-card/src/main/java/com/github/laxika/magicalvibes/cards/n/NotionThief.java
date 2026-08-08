package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.OpponentExtraDrawsRedirectedEffect;

@CardRegistration(set = "DGM", collectorNumber = "88")
public class NotionThief extends Card {

    public NotionThief() {
        addEffect(EffectSlot.STATIC, new OpponentExtraDrawsRedirectedEffect());
    }
}
