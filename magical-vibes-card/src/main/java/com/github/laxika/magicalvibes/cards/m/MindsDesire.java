package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.StormEffect;

@CardRegistration(set = "SCG", collectorNumber = "41")
@CardRegistration(set = "DDS", collectorNumber = "4")
public class MindsDesire extends Card {

    public MindsDesire() {
        addEffect(EffectSlot.SPELL, new ShuffleLibraryEffect(false));
        addEffect(EffectSlot.SPELL, new ExileTopCardMayPlayThisTurnEffect(true));
        addEffect(EffectSlot.ON_SELF_CAST, new StormEffect());
    }
}
