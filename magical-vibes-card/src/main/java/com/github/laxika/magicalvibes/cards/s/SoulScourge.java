package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.RememberTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerGainsLifeEffect;

@CardRegistration(set = "TOR", collectorNumber = "85")
public class SoulScourge extends Card {

    public SoulScourge() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new RememberTargetPlayerEffect());
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new LoseLifeEffect(3, LoseLifeRecipient.TARGET_PLAYER));
        addEffect(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD, new TargetPlayerGainsLifeEffect(3));
    }
}
