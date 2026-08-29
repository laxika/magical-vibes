package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;

@CardRegistration(set = "ELD", collectorNumber = "86")
public class EyeCollector extends Card {

    public EyeCollector() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new MillEffect(1, MillRecipient.CONTROLLER));
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new MillEffect(1, MillRecipient.EACH_OPPONENT));
    }
}
