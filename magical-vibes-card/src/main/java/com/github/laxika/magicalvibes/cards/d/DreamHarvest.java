package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachOpponentExilesTopUntilTotalManaValueMayCastThisTurnEffect;

@CardRegistration(set = "ECL", collectorNumber = "216")
@CardRegistration(set = "ECL", collectorNumber = "371")
public class DreamHarvest extends Card {

    public DreamHarvest() {
        addEffect(EffectSlot.SPELL,
                new EachOpponentExilesTopUntilTotalManaValueMayCastThisTurnEffect(5));
    }
}
