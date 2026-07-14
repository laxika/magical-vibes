package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawAndDiscardCardEffect;

import java.util.List;

@CardRegistration(set = "6ED", collectorNumber = "294")
@CardRegistration(set = "7ED", collectorNumber = "303")
public class JalumTome extends Card {

    public JalumTome() {
        // {2}, {T}: Draw a card, then discard a card.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(new DrawAndDiscardCardEffect(1, 1)),
                "{2}, {T}: Draw a card, then discard a card."
        ));
    }
}
