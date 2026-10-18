package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GivePoisonCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PoisonRecipient;
import com.github.laxika.magicalvibes.model.effect.RegisterPoisonAtNextUpkeepUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "MIR", collectorNumber = "238")
public class SabertoothCobra extends Card {

    public SabertoothCobra() {
        // Whenever this creature deals damage to a player, that player gets a poison counter. The
        // player gets another poison counter at the beginning of their next upkeep. Both halves take
        // the damaged player as their (non-targeting) target; the second half schedules a delayed
        // poison trigger at that player's next upkeep. The "unless they pay {2} before that step"
        // window is not modelled: no payment is offered, since the payment must be made before upkeep.
        addEffect(EffectSlot.ON_DAMAGE_TO_PLAYER, SequenceEffect.of(
                new GivePoisonCountersEffect(1, PoisonRecipient.TARGET_PLAYER),
                new RegisterPoisonAtNextUpkeepUnlessPaysEffect(1, "{2}")));
    }
}
