package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GivePoisonCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PoisonRecipient;
import com.github.laxika.magicalvibes.model.effect.ProliferateEffect;

@CardRegistration(set = "ONE", collectorNumber = "159")
public class BloatedContaminator extends Card {

    public BloatedContaminator() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new GivePoisonCountersEffect(1, PoisonRecipient.TARGET_PLAYER));
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new ProliferateEffect());
    }
}
