package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileRandomInstantOrSorceryFromGraveyardAndMayCastFreeEffect;
import com.github.laxika.magicalvibes.model.effect.StormEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TLE", collectorNumber = "126")
@CardRegistration(set = "TLE", collectorNumber = "199")
public class StormOfMemories extends Card {

    public StormOfMemories() {
        addSpellEffect(new ExileRandomInstantOrSorceryFromGraveyardAndMayCastFreeEffect(3));
        addTrigger(Triggers.youCastThisSpell(), new StormEffect());
    }
}
