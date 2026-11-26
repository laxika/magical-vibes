package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "M13", collectorNumber = "84")
@CardRegistration(set = "MM3", collectorNumber = "62")
public class CowerInFear extends Card {

    public CowerInFear() {
        // "Creatures your opponents control" = creatures not controlled by this card's controller.
        addSpellEffect(BoostEffect.allCreatures(-1, -1,
                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())));
    }
}
