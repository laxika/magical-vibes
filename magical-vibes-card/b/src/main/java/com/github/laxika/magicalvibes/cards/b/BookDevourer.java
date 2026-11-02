package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardOwnHandThenDrawThatManyEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "GRN", collectorNumber = "93")
public class BookDevourer extends Card {

    public BookDevourer() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(), new MayEffect(
                new DiscardOwnHandThenDrawThatManyEffect(),
                "Discard your hand and draw that many cards?"));
    }
}
