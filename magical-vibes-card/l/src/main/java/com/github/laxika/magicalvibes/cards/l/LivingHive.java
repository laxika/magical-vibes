package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MRD", collectorNumber = "124")
@CardRegistration(set = "HOP", collectorNumber = "75")
public class LivingHive extends Card {

    public LivingHive() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new CreateTokenEffect(new EventValue(), "Insect", 1, 1,
                        CardColor.GREEN, List.of(CardSubtype.INSECT), Set.of(), Set.of()));
    }
}
