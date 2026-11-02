package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.Morbid;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DoesntUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FDN", collectorNumber = "94")
public class SlumberingCerberus extends Card {

    public SlumberingCerberus() {
        // This creature doesn't untap during your untap step.
        addStaticEffect(DoesntUntapEffect.self());

        // Morbid — At the beginning of each end step, if a creature died this turn, untap this creature.
        addTrigger(Triggers.endStep(ActivePlayer.ANY), new ConditionalEffect(
                new Morbid(),
                new UntapPermanentsEffect(TapUntapScope.SELF)));
    }
}
