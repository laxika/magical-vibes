package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantAttackNextTurnCombatOpponentEffect;

@CardRegistration(set = "4ED", collectorNumber = "229")
public class WallOfDust extends Card {

    public WallOfDust() {
        // Defender is auto-loaded from Scryfall (enforced in CombatAttackService).
        // Whenever this creature blocks a creature, that creature can't attack during its
        // controller's next turn.
        addEffect(EffectSlot.ON_BLOCK, new CantAttackNextTurnCombatOpponentEffect());
    }
}
