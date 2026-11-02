package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SOI", collectorNumber = "207")
@CardRegistration(set = "SIR", collectorNumber = "197")
@CardRegistration(set = "MKC", collectorNumber = "170")
public class GrafMole extends Card {

    public GrafMole() {
        // Whenever you sacrifice a Clue, you gain 3 life.
        addTrigger(Triggers.sacrificed(new PermanentHasSubtypePredicate(CardSubtype.CLUE), ObjectRelation.YOU_CONTROL),
                new GainLifeEffect(3));
    }
}
