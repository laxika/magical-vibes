package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerMayPutLandFromHandThenOpponentsDrawEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "C16", collectorNumber = "36")
public class KynaiosAndTiroOfMeletis extends Card {

    public KynaiosAndTiroOfMeletis() {
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                SequenceEffect.of(
                        new DrawCardEffect(1),
                        new EachPlayerMayPutLandFromHandThenOpponentsDrawEffect()));
    }
}
