package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LGN", collectorNumber = "9")
public class DaruSanctifier extends Card {

    public DaruSanctifier() {
        addMorph("{1}{W}");
        target(TargetFilters.enchantment()).addTrigger(Triggers.turnedFaceUp(null, ObjectRelation.THIS),
                new DestroyTargetPermanentEffect());
    }
}
