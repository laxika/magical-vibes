package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LEG", collectorNumber = "27")
public class Lifeblood extends Card {

    public Lifeblood() {
        // Whenever a Mountain an opponent controls becomes tapped, you gain 1 life.
        addTrigger(Triggers.permanentOpponentControlsBecomesTapped()
                        .withFilter(new PermanentHasSubtypePredicate(CardSubtype.MOUNTAIN)),
                new GainLifeEffect(1));
    }
}
