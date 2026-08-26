package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "FIN", collectorNumber = "99")
public class FangFearlessLCie extends Card {

    public FangFearlessLCie() {
        addEffect(EffectSlot.ON_CONTROLLER_CARDS_LEAVE_GRAVEYARD,
                new OncePerTurnTriggerEffect(
                        SequenceEffect.of(new DrawCardEffect(1), new LoseLifeEffect(1))));
    }
}
