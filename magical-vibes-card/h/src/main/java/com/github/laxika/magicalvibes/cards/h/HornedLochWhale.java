package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.l.LagoonBreach;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ControllerTurn;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WOE", collectorNumber = "53")
@CardRegistration(set = "WOE", collectorNumber = "278")
public class HornedLochWhale extends Card {

    public HornedLochWhale() {
        setBackFaceCard(new LagoonBreach());
        addCastingOption(new AdventureCast("{1}{U}"));

        addTrigger(Triggers.becomesTargetOfOpponentSpellOrAbility(), new CounterUnlessPaysEffect(2));

        addStaticEffect(new ConditionalReplacementEffect(
                new NotCondition(new ControllerTurn()), new EntersTappedEffect()));
    }

    @Override
    public String getBackFaceClassName() {
        return "LagoonBreach";
    }
}
