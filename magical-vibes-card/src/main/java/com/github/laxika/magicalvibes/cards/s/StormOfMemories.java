package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileRandomInstantOrSorceryFromGraveyardAndMayCastFreeEffect;
import com.github.laxika.magicalvibes.model.effect.StormEffect;

@CardRegistration(set = "TLE", collectorNumber = "126")
@CardRegistration(set = "TLE", collectorNumber = "199")
public class StormOfMemories extends Card {

    public StormOfMemories() {
        addEffect(EffectSlot.SPELL,
                new ExileRandomInstantOrSorceryFromGraveyardAndMayCastFreeEffect(3));
        addEffect(EffectSlot.ON_SELF_CAST, new StormEffect());
    }
}
