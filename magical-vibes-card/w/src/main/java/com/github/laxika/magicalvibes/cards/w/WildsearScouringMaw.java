package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CascadeEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BLC", collectorNumber = "8")
@CardRegistration(set = "BLC", collectorNumber = "44")
public class WildsearScouringMaw extends Card {

    public WildsearScouringMaw() {
        addTrigger(Triggers.youCastSpell().withFilter(new CardTypePredicate(CardType.ENCHANTMENT))
                        .castFrom(Zone.HAND).grantedToTheSpell(),
                new CascadeEffect());
    }
}
