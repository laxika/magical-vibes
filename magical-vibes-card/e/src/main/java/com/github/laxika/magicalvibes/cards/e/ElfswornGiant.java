package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "FDN", collectorNumber = "103")
public class ElfswornGiant extends Card {

    public ElfswornGiant() {
        // Landfall — Whenever a land you control enters, create a 1/1 green Elf Warrior creature token.
        addTrigger(Triggers.enters(Triggers.LAND, ObjectRelation.YOU_CONTROL),
                new CreateTokenEffect("Elf Warrior", 1, 1,
                        CardColor.GREEN, List.of(CardSubtype.ELF, CardSubtype.WARRIOR),
                        Set.of(), Set.of()));
    }
}
