package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "HOC", collectorNumber = "181")
public class NimrodelWatcher extends Card {

    public NimrodelWatcher() {
        addEffect(EffectSlot.ON_CONTROLLER_SCRIES, new OncePerTurnTriggerEffect(
                SequenceEffect.of(
                        new BoostSelfEffect(1, 0),
                        new MakeCreatureUnblockableEffect(true))));
    }
}
