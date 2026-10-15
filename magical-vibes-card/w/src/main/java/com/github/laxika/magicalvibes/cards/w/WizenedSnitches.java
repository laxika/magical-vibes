package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PlayWithTopCardRevealedEffect;

@CardRegistration(set = "RAV", collectorNumber = "75")
public class WizenedSnitches extends Card {

    public WizenedSnitches() {
        addEffect(EffectSlot.STATIC, PlayWithTopCardRevealedEffect.forAllPlayers());
    }
}
