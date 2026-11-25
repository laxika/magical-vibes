package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.DelveCost;

@CardRegistration(set = "FUT", collectorNumber = "52")
@CardRegistration(set = "MMA", collectorNumber = "51")
@CardRegistration(set = "TSR", collectorNumber = "73")
public class LogicKnot extends Card {

    public LogicKnot() {
        addCastingCost(new DelveCost());
        addSpellEffect(UnlessPaysEffect.counterUnlessPaysX());
    }
}
