package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.StrongholdGambitEffect;

@CardRegistration(set = "NEM", collectorNumber = "100")
public class StrongholdGambit extends Card {

    public StrongholdGambit() {
        addEffect(EffectSlot.SPELL, new StrongholdGambitEffect());
    }
}
