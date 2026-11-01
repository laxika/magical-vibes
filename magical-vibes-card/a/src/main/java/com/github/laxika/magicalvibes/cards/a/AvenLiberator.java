package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GrantProtectionChoiceUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SCG", collectorNumber = "4")
public class AvenLiberator extends Card {

    public AvenLiberator() {
        addMorph("{3}{W}");
        target(TargetFilters.creatureYouControl())
                .addTrigger(Triggers.turnedFaceUp(null, ObjectRelation.THIS),
                        new GrantProtectionChoiceUntilEndOfTurnEffect());
    }
}
