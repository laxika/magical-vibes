package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;

@CardRegistration(set = "5DN", collectorNumber = "55")
@CardRegistration(set = "DDM", collectorNumber = "68")
@CardRegistration(set = "EMA", collectorNumber = "100")
@CardRegistration(set = "SLD", collectorNumber = "207")
@CardRegistration(set = "SLD", collectorNumber = "1937")
public class NightsWhisper extends Card {

    public NightsWhisper() {
        addEffect(EffectSlot.SPELL, new DrawCardEffect(2));
        addEffect(EffectSlot.SPELL, new LoseLifeEffect(2));
    }
}
