package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SLD", collectorNumber = "1963")
@CardRegistration(set = "MH2", collectorNumber = "214")
public class SythisHarvestsHand extends Card {

    public SythisHarvestsHand() {
        // Whenever you cast an enchantment spell, you gain 1 life and draw a card.
        addTrigger(Triggers.youCastSpell().withFilter(new CardTypePredicate(CardType.ENCHANTMENT)),
                new GainLifeEffect(1),
                new DrawCardEffect(1));
    }
}
