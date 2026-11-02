package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CopySpellForEachOtherControlledCreatureEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "INR", collectorNumber = "165")
@CardRegistration(set = "EMN", collectorNumber = "136")
@CardRegistration(set = "SIR", collectorNumber = "170")
@CardRegistration(set = "SOC", collectorNumber = "249")
@CardRegistration(set = "TLE", collectorNumber = "33")
public class MirrorwingDragon extends Card {

    public MirrorwingDragon() {
        // Whenever a player casts an instant or sorcery spell that targets only this creature,
        // that player copies that spell for each other creature they control that the spell could target.
        // Each copy targets a different one of those creatures.
        addTrigger(Triggers.playerCastsSpell(),
                new CopySpellForEachOtherControlledCreatureEffect());
    }
}
