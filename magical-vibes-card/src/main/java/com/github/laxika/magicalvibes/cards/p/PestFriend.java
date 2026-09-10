package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

import java.util.List;
import java.util.Map;
import java.util.Set;

/** Pest Friend, the prepare spell of Lluwen, Exchange Student // Pest Friend (SOS 199). */
public class PestFriend extends Card {

    public PestFriend() {
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                CardType.CREATURE,
                1,
                "Pest",
                1,
                1,
                CardColor.BLACK,
                Set.of(CardColor.BLACK, CardColor.GREEN),
                List.of(CardSubtype.PEST),
                Set.of(),
                Set.of(),
                false,
                false,
                Map.of(EffectSlot.ON_ATTACK, new GainLifeEffect(1)),
                List.of(),
                false,
                false,
                false,
                0,
                Set.of()
        ));
    }
}
