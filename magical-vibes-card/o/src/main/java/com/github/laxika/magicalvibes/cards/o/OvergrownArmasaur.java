package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "RIX", collectorNumber = "141")
public class OvergrownArmasaur extends Card {

    public OvergrownArmasaur() {
        addTrigger(Triggers.dealtDamage(), new CreateTokenEffect(
                "Saproling", 1, 1,
                CardColor.GREEN,
                List.of(CardSubtype.SAPROLING),
                Set.of(),
                Set.of()
        ));
    }
}
