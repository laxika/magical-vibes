package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardsOfTargetPlayerUntilInstantOrSorceryAndCastEffect;

@CardRegistration(set = "C21", collectorNumber = "25")
@CardRegistration(set = "OTC", collectorNumber = "93")
public class DazzlingSphinx extends Card {

    public DazzlingSphinx() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new RevealTopCardsOfTargetPlayerUntilInstantOrSorceryAndCastEffect());
    }
}
