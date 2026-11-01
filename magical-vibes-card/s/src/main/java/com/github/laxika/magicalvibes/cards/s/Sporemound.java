package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "M14", collectorNumber = "196")
@CardRegistration(set = "SCD", collectorNumber = "212")
@CardRegistration(set = "ZNC", collectorNumber = "82")
public class Sporemound extends Card {

    public Sporemound() {
        // Landfall — Whenever a land you control enters, create a 1/1 green Saproling creature token.
        addTrigger(Triggers.enters(Triggers.LAND, ObjectRelation.YOU_CONTROL),
                new CreateTokenEffect("Saproling", 1, 1,
                        CardColor.GREEN, List.of(CardSubtype.SAPROLING),
                        Set.of(), Set.of()));
    }
}
