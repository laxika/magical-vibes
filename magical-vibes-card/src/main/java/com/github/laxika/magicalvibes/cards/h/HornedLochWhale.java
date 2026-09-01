package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.l.LagoonBreach;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerTurn;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;

@CardRegistration(set = "WOE", collectorNumber = "53")
public class HornedLochWhale extends Card {

    public HornedLochWhale() {
        setBackFaceCard(new LagoonBreach());
        addCastingOption(new AdventureCast("{1}{U}"));

        addEffect(EffectSlot.STATIC, new ConditionalReplacementEffect(
                new NotCondition(new ControllerTurn()), new EntersTappedEffect()));
    }

    @Override
    public String getBackFaceClassName() {
        return "LagoonBreach";
    }
}
