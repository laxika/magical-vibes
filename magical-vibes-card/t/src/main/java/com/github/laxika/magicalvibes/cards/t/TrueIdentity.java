package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MKC", collectorNumber = "16")
@CardRegistration(set = "MKC", collectorNumber = "327")
public class TrueIdentity extends Card {

    public TrueIdentity() {
        addMorph("{W}");
        addTrigger(Triggers.turnedFaceUp(null, ObjectRelation.YOU_CONTROL),
                new OncePerTurnTriggerEffect(SequenceEffect.of(
                        new ScryEffect(1),
                        new DrawCardEffect(1))));
    }
}
