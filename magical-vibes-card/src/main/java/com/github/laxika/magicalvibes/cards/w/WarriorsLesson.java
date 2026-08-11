package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToTargetUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "THS", collectorNumber = "184")
public class WarriorsLesson extends Card {

    public WarriorsLesson() {
        // Up to two target creatures you control gain a combat-damage-to-player draw trigger until
        // end of turn.
        target(TargetFilters.creatureYouControl(), 0, 2)
                .addEffect(EffectSlot.SPELL, new GrantEffectToTargetUntilEndOfTurnEffect(
                        EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new DrawCardEffect(1)));
    }
}
