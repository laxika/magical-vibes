package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;

@CardRegistration(set = "ZNR", collectorNumber = "143")
public class GrotagNightRunner extends Card {

    public GrotagNightRunner() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new ExileTopCardMayPlayThisTurnEffect(false));
    }
}
