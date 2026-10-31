package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedControllerSpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "CHK", collectorNumber = "210")
@CardRegistration(set = "SLC", collectorNumber = "2004")
@CardRegistration(set = "SOA", collectorNumber = "53")
public class GlimpseOfNature extends Card {

    public GlimpseOfNature() {
        // Whenever you cast a creature spell this turn, draw a card.
        addSpellEffect(new RegisterDelayedControllerSpellCastTriggerEffect(
                new CardTypePredicate(CardType.CREATURE), List.of(new DrawCardEffect(1)), false, false));
    }
}
