package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MOR", collectorNumber = "113")
@CardRegistration(set = "DDS", collectorNumber = "42")
@CardRegistration(set = "A25", collectorNumber = "158")
public class AmbassadorOak extends Card {

    public AmbassadorOak() {
        // When this creature enters, create a 1/1 green Elf Warrior creature token.
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new CreateTokenEffect("Elf Warrior", 1, 1, CardColor.GREEN,
                        List.of(CardSubtype.ELF, CardSubtype.WARRIOR), Set.of(), Set.of()));
    }
}
