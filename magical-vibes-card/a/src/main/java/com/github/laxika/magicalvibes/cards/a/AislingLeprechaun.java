package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.effect.SetTargetColorEffect;

@CardRegistration(set = "LEG", collectorNumber = "173")
public class AislingLeprechaun extends Card {

    public AislingLeprechaun() {
        addEffect(EffectSlot.ON_BLOCK, new SetTargetColorEffect(CardColor.GREEN));
        addEffect(EffectSlot.ON_BECOMES_BLOCKED, new SetTargetColorEffect(CardColor.GREEN),
                TriggerMode.PER_BLOCKER);
    }
}
