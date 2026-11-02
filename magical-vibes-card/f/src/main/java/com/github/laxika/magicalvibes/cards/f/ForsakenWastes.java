package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.PlayersCantGainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.TargetSpellControllerLosesLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MIR", collectorNumber = "125")
public class ForsakenWastes extends Card {

    public ForsakenWastes() {
        addStaticEffect(new PlayersCantGainLifeEffect());

        // Triggers.upkeep(ActivePlayer.ANY) sets the active player as the target, so "that player loses 1 life".
        addTrigger(Triggers.upkeep(ActivePlayer.ANY), new LoseLifeEffect(1, LoseLifeRecipient.ACTIVE_PLAYER));

        // The trigger is queued against the spell that targeted this enchantment (STACK zone), so the
        // life loss hits that spell's controller.
        addTrigger(Triggers.becomesTargetOfSpell(), new TargetSpellControllerLosesLifeEffect(5));
    }
}
