package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.TapOrUntapTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MKM", collectorNumber = "206")
public class GraniteWitness extends Card {

    public GraniteWitness() {
        addMorph("{W/U}{W/U}");
        target(TargetFilters.creature()).addTrigger(Triggers.turnedFaceUp(null, ObjectRelation.THIS),
                new MayEffect(new TapOrUntapTargetPermanentEffect(),
                        "Tap or untap target creature?"));
    }
}
