package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.SpellCastTimingRestriction;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.FlipCoinWinEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "INV", collectorNumber = "140")
public class ChaoticStrike extends Card {

    public ChaoticStrike() {
        // Cast this spell only during combat after blockers are declared.
        setSpellCastTimingRestriction(SpellCastTimingRestriction.COMBAT_AFTER_BLOCKERS);

        // Flip a coin. If you win the flip, target creature gets +1/+1 until end of turn.
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new FlipCoinWinEffect(new BoostTargetCreatureEffect(1, 1)));

        // Draw a card.
        addEffect(EffectSlot.SPELL, new DrawCardEffect());
    }
}
