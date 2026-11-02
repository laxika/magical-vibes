package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CopySpellForEachOtherCreatureWithManaEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MKC", collectorNumber = "6")
@CardRegistration(set = "MKC", collectorNumber = "313")
public class FeatherRadiantArbiter extends Card {

    public FeatherRadiantArbiter() {
        addTrigger(Triggers.youCastSpell(),
                new CopySpellForEachOtherCreatureWithManaEffect("{2}"));
    }
}
