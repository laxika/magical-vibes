package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerLifeAtMost;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DKA", collectorNumber = "8")
public class GatherTheTownsfolk extends Card {

    public GatherTheTownsfolk() {
        // Create two 1/1 white Human creature tokens.
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(2, "Human", 1, 1,
                CardColor.WHITE, List.of(CardSubtype.HUMAN), Set.of(), Set.of()));

        // Fateful hour — If you have 5 or less life, create five of those tokens instead
        // (i.e. three additional tokens on top of the two above).
        addEffect(EffectSlot.SPELL, new ConditionalEffect(new ControllerLifeAtMost(5), new CreateTokenEffect(3, "Human", 1, 1,
                        CardColor.WHITE, List.of(CardSubtype.HUMAN), Set.of(), Set.of())));
    }
}
