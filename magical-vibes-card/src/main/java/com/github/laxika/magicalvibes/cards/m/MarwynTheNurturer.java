package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEqualToSourcePowerEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SubtypeConditionalEffect;

import java.util.List;

@CardRegistration(set = "DOM", collectorNumber = "172")
public class MarwynTheNurturer extends Card {

    public MarwynTheNurturer() {
        // Whenever another Elf you control enters, put a +1/+1 counter on Marwyn, the Nurturer.
        addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new SubtypeConditionalEffect(CardSubtype.ELF,
                        new PutCountersOnSourceEffect(1, 1, 1)));

        // {T}: Add an amount of {G} equal to Marwyn's power.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEqualToSourcePowerEffect(ManaColor.GREEN)),
                "{T}: Add an amount of {G} equal to Marwyn's power."
        ));
    }
}
