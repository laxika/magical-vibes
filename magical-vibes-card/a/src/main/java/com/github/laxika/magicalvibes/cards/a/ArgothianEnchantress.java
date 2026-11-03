package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "USG", collectorNumber = "234")
@CardRegistration(set = "EMA", collectorNumber = "158")
public class ArgothianEnchantress extends Card {

    public ArgothianEnchantress() {
        // Whenever you cast an enchantment spell, draw a card.
        addTrigger(Triggers.youCastSpell().withFilter(new CardTypePredicate(CardType.ENCHANTMENT)),
                new DrawCardEffect());
    }
}
