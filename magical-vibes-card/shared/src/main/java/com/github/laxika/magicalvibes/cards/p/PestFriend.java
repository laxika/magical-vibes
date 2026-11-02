package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

/** Pest Friend, the prepare spell of Lluwen, Exchange Student // Pest Friend (SOS 199). */
public class PestFriend extends Card {

    public PestFriend() {
        addSpellEffect(new CreateTokenEffect(
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
                List.of(new TriggeredAbility(Triggers.attacks(), new GainLifeEffect(1))),
                List.of(),
                false,
                false,
                false,
                0,
                Set.of()
        ));
    }
}
