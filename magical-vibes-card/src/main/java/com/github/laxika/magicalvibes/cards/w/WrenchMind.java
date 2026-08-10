package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;

@CardRegistration(set = "MRD", collectorNumber = "84")
public class WrenchMind extends Card {

    public WrenchMind() {
        addEffect(EffectSlot.SPELL, new DiscardEffect(2, DiscardRecipient.TARGET_PLAYER, CardType.ARTIFACT));
    }
}
