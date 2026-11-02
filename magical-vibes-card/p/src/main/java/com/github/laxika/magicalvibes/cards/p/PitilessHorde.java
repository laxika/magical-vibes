package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSelfToHandIfDashCostPaidEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "DTK", collectorNumber = "112")
public class PitilessHorde extends Card {

    public PitilessHorde() {
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{2}{B}{B}"))));
        addTrigger(Triggers.enters(ObjectRelation.THIS), new ReturnSelfToHandIfDashCostPaidEffect());
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new LoseLifeEffect(2));
    }
}
