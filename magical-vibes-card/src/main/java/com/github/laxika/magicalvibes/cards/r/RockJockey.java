package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.AllConditions;
import com.github.laxika.magicalvibes.model.condition.DidntPlayLandThisTurn;
import com.github.laxika.magicalvibes.model.condition.SourceEnteredThisTurn;
import com.github.laxika.magicalvibes.model.condition.WasCast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ControllerCantPlayLandsEffect;

import java.util.List;

@CardRegistration(set = "SCG", collectorNumber = "101")
public class RockJockey extends Card {

    public RockJockey() {
        setCastCondition(new DidntPlayLandThisTurn());
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new AllConditions(List.of(new WasCast(), new SourceEnteredThisTurn())),
                new ControllerCantPlayLandsEffect()));
    }
}
