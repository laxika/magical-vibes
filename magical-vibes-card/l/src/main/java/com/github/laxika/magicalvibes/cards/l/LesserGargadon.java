package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "8ED", collectorNumber = "199")
@CardRegistration(set = "PCY", collectorNumber = "97")
public class LesserGargadon extends Card {

    public LesserGargadon() {
        // Whenever this creature attacks or blocks, sacrifice a land.
        addTrigger(List.of(Triggers.attacks(), Triggers.blocks()),
                new SacrificePermanentsEffect(1, new PermanentIsLandPredicate(), SacrificeRecipient.CONTROLLER));
    }
}
