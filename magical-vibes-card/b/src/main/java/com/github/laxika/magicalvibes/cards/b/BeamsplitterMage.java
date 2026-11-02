package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CopySpellForEachOtherControlledCreatureEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "GRN", collectorNumber = "155")
public class BeamsplitterMage extends Card {

    public BeamsplitterMage() {
        addTrigger(Triggers.youCastSpell(),
                new CopySpellForEachOtherControlledCreatureEffect(true));
    }
}
