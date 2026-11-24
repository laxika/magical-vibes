package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ControllerTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EndTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnColoredCastCostEffect;

@CardRegistration(set = "M21", collectorNumber = "48")
@CardRegistration(set = "M21", collectorNumber = "349")
public class Discontinuity extends Card {

    public Discontinuity() {
        addStaticEffect(new ConditionalEffect(new ControllerTurn(),
                new ReduceOwnColoredCastCostEffect("{2}{U}{U}")));
        addSpellEffect(new EndTurnEffect());
    }
}
