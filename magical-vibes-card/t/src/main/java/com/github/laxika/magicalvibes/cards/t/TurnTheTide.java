package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "MBS", collectorNumber = "35")
@CardRegistration(set = "TD2", collectorNumber = "30")
public class TurnTheTide extends Card {

    public TurnTheTide() {
        addSpellEffect(BoostEffect.allCreatures(-2, 0,
                        new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())));
    }
}
