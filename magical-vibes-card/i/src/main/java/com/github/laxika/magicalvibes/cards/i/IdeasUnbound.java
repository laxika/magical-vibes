package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedStepTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SOK", collectorNumber = "40")
public class IdeasUnbound extends Card {

    public IdeasUnbound() {
        addSpellEffect(new DrawCardEffect(3));
        addSpellEffect(RegisterDelayedStepTriggerEffect.atNext(
                Triggers.endStep(ActivePlayer.ANY), new DiscardEffect(3, Subjects.you())));
    }
}
