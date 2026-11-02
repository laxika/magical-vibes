package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MMQ", collectorNumber = "267")
public class SaberAnts extends Card {

    public SaberAnts() {
        // Whenever this creature is dealt damage, you may create that many 1/1 green Insect
        // creature tokens.
        addTrigger(Triggers.dealtDamage(), new MayEffect(
                new CreateTokenEffect(new EventValue(), "Insect", 1, 1, CardColor.GREEN,
                        List.of(CardSubtype.INSECT), Set.of(), Set.of()),
                "Create that many 1/1 green Insect creature tokens?"));
    }
}
