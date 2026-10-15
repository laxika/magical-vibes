package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "MKC", collectorNumber = "16")
@CardRegistration(set = "MKC", collectorNumber = "327")
public class TrueIdentity extends Card {

    public TrueIdentity() {
        addMorph("{W}");
        addEffect(EffectSlot.ON_SELF_OR_ALLY_PERMANENT_TURNS_FACE_UP,
                new OncePerTurnTriggerEffect(SequenceEffect.of(
                        new ScryEffect(1),
                        new DrawCardEffect(1))));
    }
}
