package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "RAV", collectorNumber = "177")
@CardRegistration(set = "C14", collectorNumber = "211")
@CardRegistration(set = "CMA", collectorNumber = "138")
public class PrimordialSage extends Card {

    public PrimordialSage() {
        // Whenever you cast a creature spell, you may draw a card.
        addTrigger(Triggers.youCastSpell(), new SpellCastTriggerEffect(
                new CardTypePredicate(CardType.CREATURE),
                List.of(new MayEffect(new DrawCardEffect(), "Draw a card?"))
        ));
    }
}
