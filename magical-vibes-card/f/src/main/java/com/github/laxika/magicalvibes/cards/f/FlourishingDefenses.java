package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SHM", collectorNumber = "114")
@CardRegistration(set = "ECC", collectorNumber = "107")
public class FlourishingDefenses extends Card {

    public FlourishingDefenses() {
        // Whenever a -1/-1 counter is put on a creature, you may create a 1/1 green Elf Warrior
        // creature token. Fires once per individual counter (Gatherer ruling), including when a
        // creature enters with -1/-1 counters.
        addTrigger(Triggers.minusOneCounterPutOnCreature(),
                new MayEffect(
                        new CreateTokenEffect("Elf Warrior", 1, 1, CardColor.GREEN,
                                List.of(CardSubtype.ELF, CardSubtype.WARRIOR), Set.of(), Set.of()),
                        "Create a 1/1 green Elf Warrior creature token?"));
    }
}
