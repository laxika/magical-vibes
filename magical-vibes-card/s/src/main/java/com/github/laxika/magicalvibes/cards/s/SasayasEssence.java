package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AddManaForEachOtherLandWithSameNameEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

public class SasayasEssence extends Card {

    public SasayasEssence() {
        addTrigger(Triggers.playerTapsLandForMana(),
                new AddManaForEachOtherLandWithSameNameEffect());
    }
}
