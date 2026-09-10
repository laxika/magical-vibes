package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;

@CardRegistration(set = "VOW", collectorNumber = "237")
public class EruthTormentedProphet extends Card {

    public EruthTormentedProphet() {
        addEffect(EffectSlot.STATIC, new ExileTopCardMayPlayThisTurnEffect(2, false));
    }
}
