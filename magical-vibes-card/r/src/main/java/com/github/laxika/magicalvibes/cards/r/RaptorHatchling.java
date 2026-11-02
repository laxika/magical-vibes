package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "XLN", collectorNumber = "155")
public class RaptorHatchling extends Card {

    public RaptorHatchling() {
        // Enrage — Whenever this creature is dealt damage, create a 3/3 green Dinosaur creature token with trample.
        addTrigger(Triggers.dealtDamage(), new CreateTokenEffect(
                "Dinosaur", 3, 3,
                CardColor.GREEN,
                List.of(CardSubtype.DINOSAUR),
                Set.of(Keyword.TRAMPLE),
                Set.of()
        ));
    }
}
