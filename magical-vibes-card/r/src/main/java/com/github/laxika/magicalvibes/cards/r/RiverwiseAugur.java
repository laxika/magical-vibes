package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerPutsCardsFromHandOnTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.HandToLibraryPlacement;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "RIX", collectorNumber = "48")
@CardRegistration(set = "AFC", collectorNumber = "93")
public class RiverwiseAugur extends Card {

    public RiverwiseAugur() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new DrawCardEffect(3),
                        TargetPlayerPutsCardsFromHandOnTopOfLibraryEffect.yours(2, HandToLibraryPlacement.TOP));
    }
}
