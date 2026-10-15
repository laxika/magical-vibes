package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachPlayerChoosesOneOrTwoCreaturesCreatesTokenCopyEffect;

@CardRegistration(set = "WHO", collectorNumber = "585")
public class HumanTimeLordMetaCrisis extends Card {

    public HumanTimeLordMetaCrisis() {
        addEffect(EffectSlot.ENCOUNTER_TRIGGERED,
                new EachPlayerChoosesOneOrTwoCreaturesCreatesTokenCopyEffect());
    }
}
