package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.PlayersCantGainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.TargetSpellControllerLosesLifeEffect;

@CardRegistration(set = "MIR", collectorNumber = "125")
public class ForsakenWastes extends Card {

    public ForsakenWastes() {
        addEffect(EffectSlot.STATIC, new PlayersCantGainLifeEffect());

        // EACH_UPKEEP_TRIGGERED sets the active player as the target, so "that player loses 1 life".
        addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED, new LoseLifeEffect(1, LoseLifeRecipient.TARGET_PLAYER));

        // The trigger is queued against the spell that targeted this enchantment (STACK zone), so the
        // life loss hits that spell's controller.
        addEffect(EffectSlot.ON_BECOMES_TARGET_OF_SPELL, new TargetSpellControllerLosesLifeEffect(5));
    }
}
