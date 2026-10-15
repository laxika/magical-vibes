package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PlayWithTopCardRevealedEffect;

@CardRegistration(set = "LEG", collectorNumber = "55")
public class FieldOfDreams extends Card {

    public FieldOfDreams() {
        addEffect(EffectSlot.STATIC, PlayWithTopCardRevealedEffect.forAllPlayers());
    }
}
