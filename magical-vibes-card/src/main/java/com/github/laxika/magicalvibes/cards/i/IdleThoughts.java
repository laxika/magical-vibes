package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ControllerHandEmpty;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

import java.util.List;

@CardRegistration(set = "EVE", collectorNumber = "23")
public class IdleThoughts extends Card {

    public IdleThoughts() {
        // {2}: Draw a card if you have no cards in hand. The empty-hand check happens on
        // resolution; the ability costs no cards, so an empty hand at activation still holds.
        addActivatedAbility(new ActivatedAbility(false, "{2}",
                List.of(new ConditionalEffect(new ControllerHandEmpty(), new DrawCardEffect(1))),
                "{2}: Draw a card if you have no cards in hand."));
    }
}
