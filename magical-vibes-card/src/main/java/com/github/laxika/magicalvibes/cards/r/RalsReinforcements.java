package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MOM", collectorNumber = "158")
public class RalsReinforcements extends Card {

    public RalsReinforcements() {
        // Create two 1/1 blue and red Elemental creature tokens.
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(2, "Elemental", 1, 1, null,
                Set.of(CardColor.BLUE, CardColor.RED), List.of(CardSubtype.ELEMENTAL)));
    }
}
