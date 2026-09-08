package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileCardFromAnyGraveyardThenMayBecomeCopyEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "OTJ", collectorNumber = "216")
public class LazavFamiliarStranger extends Card {

    public LazavFamiliarStranger() {
        addEffect(EffectSlot.ON_CONTROLLER_COMMITS_CRIME,
                new OncePerTurnTriggerEffect(SequenceEffect.of(
                        new PutCountersOnSourceEffect(1, 1, 1),
                        new ExileCardFromAnyGraveyardThenMayBecomeCopyEffect())));
    }
}
