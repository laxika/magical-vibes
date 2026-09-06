package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachPlayerReturnsCreaturesOfChosenTypeFromGraveyardEffect;

@CardRegistration(set = "ONS", collectorNumber = "161")
public class PatriarchsBidding extends Card {

    public PatriarchsBidding() {
        addEffect(EffectSlot.SPELL, new EachPlayerReturnsCreaturesOfChosenTypeFromGraveyardEffect());
    }
}
