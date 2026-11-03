package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SOM", collectorNumber = "16")
@CardRegistration(set = "MM2", collectorNumber = "28")
@CardRegistration(set = "2XM", collectorNumber = "23")
@CardRegistration(set = "CMM", collectorNumber = "44")
@CardRegistration(set = "NEC", collectorNumber = "86")
public class Myrsmith extends Card {

    public Myrsmith() {
        addTrigger(Triggers.youCastSpell().withFilter(new CardTypePredicate(CardType.ARTIFACT)),
                new MayPayManaEffect("{1}", new CreateTokenEffect("Myr", 1, 1, null,
                        List.of(CardSubtype.MYR), Set.of(), Set.of(CardType.ARTIFACT)), "Pay {1} to create a 1/1 colorless Myr artifact creature token?"));
    }
}
