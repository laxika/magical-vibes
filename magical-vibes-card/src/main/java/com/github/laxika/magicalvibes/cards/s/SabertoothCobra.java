package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GivePoisonCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PoisonRecipient;
import com.github.laxika.magicalvibes.model.effect.RegisterPoisonAtNextUpkeepUnlessPaysEffect;

@CardRegistration(set = "MIR", collectorNumber = "238")
public class SabertoothCobra extends Card {

    public SabertoothCobra() {
        // Whenever this creature deals damage to a player, that player gets a poison counter. The
        // player gets another poison counter at the beginning of their next upkeep unless they pay
        // {2} before that step. Both halves take the damaged player as their (non-targeting) target;
        // the second half schedules a delayed pay-or-poison obligation at that player's next upkeep.
        addEffect(EffectSlot.ON_DAMAGE_TO_PLAYER, new GivePoisonCountersEffect(1, PoisonRecipient.TARGET_PLAYER));
        addEffect(EffectSlot.ON_DAMAGE_TO_PLAYER, new RegisterPoisonAtNextUpkeepUnlessPaysEffect(1, "{2}"));
    }
}
