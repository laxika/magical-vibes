package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerHasMoreLifeThanAnOpponent;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MOR", collectorNumber = "9")
public class FeudkillersVerdict extends Card {

    public FeudkillersVerdict() {
        // You gain 10 life.
        addEffect(EffectSlot.SPELL, new GainLifeEffect(10));
        // Then if you have more life than an opponent, create a 5/5 white Giant Warrior creature token.
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new ControllerHasMoreLifeThanAnOpponent(),
                new CreateTokenEffect("Giant Warrior", 5, 5, CardColor.WHITE,
                        List.of(CardSubtype.GIANT, CardSubtype.WARRIOR), Set.of(), Set.of())));
    }
}
