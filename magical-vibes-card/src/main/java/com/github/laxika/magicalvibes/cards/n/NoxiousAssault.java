package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.GivePoisonCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PoisonRecipient;
import com.github.laxika.magicalvibes.model.effect.RegisterGlobalTriggeredAbilityUntilEndOfTurnEffect;

@CardRegistration(set = "ONE", collectorNumber = "176")
public class NoxiousAssault extends Card {

    public NoxiousAssault() {
        addEffect(EffectSlot.SPELL, new BoostAllOwnCreaturesEffect(2, 2));
        addEffect(EffectSlot.SPELL, new RegisterGlobalTriggeredAbilityUntilEndOfTurnEffect(
                EffectSlot.ON_ANY_CREATURE_BLOCKS,
                new GivePoisonCountersEffect(1, PoisonRecipient.TARGET_PERMANENT_CONTROLLER)));
    }
}
