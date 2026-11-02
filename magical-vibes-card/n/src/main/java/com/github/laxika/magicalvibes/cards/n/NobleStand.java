package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "NEM", collectorNumber = "14")
public class NobleStand extends Card {

    public NobleStand() {
        addTrigger(Triggers.creatureBlocks(),
                new TriggeringPermanentConditionalEffect(
                        new PermanentControlledBySourceControllerPredicate(),
                        new GainLifeEffect(2)));
    }
}
