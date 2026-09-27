package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfEnteringTokenForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;

@CardRegistration(set = "SLD", collectorNumber = "2447")
public class LucyMacLeanPositivelyArmed extends Card {

    public LucyMacLeanPositivelyArmed() {
        var ability = new OncePerTurnTriggerEffect(
                new CreateTokenCopyOfEnteringTokenForTargetPlayerEffect());
        addEffect(EffectSlot.ON_ALLY_TOKEN_ENTERS_BATTLEFIELD, ability);
        addEffect(EffectSlot.ON_OPPONENT_TOKEN_ENTERS_BATTLEFIELD, ability);
    }
}
