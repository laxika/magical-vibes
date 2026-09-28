package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.OpponentExtraDrawsRedirectedEffect;

@CardRegistration(set = "DGM", collectorNumber = "88")
@CardRegistration(set = "A25", collectorNumber = "211")
@CardRegistration(set = "PIO", collectorNumber = "235")
@CardRegistration(set = "SPG", collectorNumber = "36")
@CardRegistration(set = "LTC", collectorNumber = "270")
@CardRegistration(set = "SLD", collectorNumber = "2482")
public class NotionThief extends Card {

    public NotionThief() {
        addEffect(EffectSlot.STATIC, new OpponentExtraDrawsRedirectedEffect());
    }
}
