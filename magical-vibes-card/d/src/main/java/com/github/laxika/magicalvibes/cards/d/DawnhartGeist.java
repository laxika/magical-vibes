package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "VOW", collectorNumber = "8")
@CardRegistration(set = "DBL", collectorNumber = "275")
public class DawnhartGeist extends Card {

    public DawnhartGeist() {
        addTrigger(Triggers.youCastSpell().withFilter(new CardTypePredicate(CardType.ENCHANTMENT)),
                new GainLifeEffect(2));
    }
}
