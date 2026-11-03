package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "USG", collectorNumber = "32")
@CardRegistration(set = "LEG", collectorNumber = "31")
public class PresenceOfTheMaster extends Card {

    public PresenceOfTheMaster() {
        addTrigger(Triggers.playerCastsSpell().withFilter(new CardTypePredicate(CardType.ENCHANTMENT)),
                new CounterSpellEffect());
    }
}
