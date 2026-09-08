package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ReturnPermanentsCost;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "MMQ", collectorNumber = "108")
public class Thwart extends Card {

    public Thwart() {
        // You may return three Islands you control to their owner's hand rather than pay this spell's mana cost.
        addCastingOption(new AlternateHandCast(List.of(
                new ReturnPermanentsCost(3, new PermanentHasSubtypePredicate(CardSubtype.ISLAND))
        )));

        // Counter target spell.
        addEffect(EffectSlot.SPELL, new CounterSpellEffect());
    }
}
