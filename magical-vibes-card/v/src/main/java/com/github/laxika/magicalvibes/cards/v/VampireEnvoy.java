package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "OGW", collectorNumber = "92")
public class VampireEnvoy extends Card {

    public VampireEnvoy() {
        // Whenever this creature becomes tapped, you gain 1 life.
        addTrigger(Triggers.permanentYouControlBecomesTapped(), new TriggeringPermanentConditionalEffect(
                new PermanentIsSourceCardPredicate(),
                new GainLifeEffect(1)));
    }
}
