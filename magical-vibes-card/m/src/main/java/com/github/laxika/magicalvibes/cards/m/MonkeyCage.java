package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.TriggeringPermanentManaValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MMQ", collectorNumber = "307")
public class MonkeyCage extends Card {

    public MonkeyCage() {
        // When a creature enters, sacrifice this artifact and create X 2/2 green Monkey creature tokens,
        // where X is that creature's mana value.
        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER),
                SequenceEffect.of(new SacrificeSelfEffect(),
                        new CreateTokenEffect("Monkey", 2, 2, CardColor.GREEN,
                                List.of(CardSubtype.MONKEY), Set.of(), Set.of())
                                .withAmount(new TriggeringPermanentManaValue())));
    }
}
