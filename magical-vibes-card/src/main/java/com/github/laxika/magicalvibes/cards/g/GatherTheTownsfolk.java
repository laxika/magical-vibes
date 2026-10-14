package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.FixedIfCondition;
import com.github.laxika.magicalvibes.model.condition.ControllerLifeAtMost;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DKA", collectorNumber = "8")
@CardRegistration(set = "DDQ", collectorNumber = "12")
@CardRegistration(set = "INR", collectorNumber = "23")
@CardRegistration(set = "INR", collectorNumber = "341")
public class GatherTheTownsfolk extends Card {

    public GatherTheTownsfolk() {
        // Create two 1/1 white Human creature tokens. Fateful hour — If you have 5 or less life, create
        // five of those tokens instead. Both counts come from one creation event so that the tokens
        // enter together.
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                new FixedIfCondition(new ControllerLifeAtMost(5), 5, 2), "Human", 1, 1,
                CardColor.WHITE, List.of(CardSubtype.HUMAN), Set.of(), Set.of()));
    }
}
