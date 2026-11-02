package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CantAttackNextTurnCombatOpponentEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "4ED", collectorNumber = "229")
@CardRegistration(set = "LEG", collectorNumber = "168")
public class WallOfDust extends Card {

    public WallOfDust() {
        // Defender is auto-loaded from Scryfall (enforced in CombatAttackService).
        // Whenever this creature blocks a creature, that creature can't attack during its
        // controller's next turn.
        addTrigger(Triggers.blocks(), new CantAttackNextTurnCombatOpponentEffect());
    }
}
