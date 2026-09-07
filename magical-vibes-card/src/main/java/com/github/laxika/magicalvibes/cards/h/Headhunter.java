package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;

@CardRegistration(set = "ONS", collectorNumber = "156")
public class Headhunter extends Card {

    public Headhunter() {
        addMorph("{B}");
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new DiscardEffect(1, DiscardRecipient.TARGET_PLAYER));
    }
}
