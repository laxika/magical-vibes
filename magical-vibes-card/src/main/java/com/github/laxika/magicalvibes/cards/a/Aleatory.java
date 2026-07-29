package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.SpellCastTimingRestriction;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.FlipCoinWinEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDrawCardsAtNextUpkeepEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MIR", collectorNumber = "155")
public class Aleatory extends Card {

    public Aleatory() {
        // Cast this spell only during combat after blockers are declared.
        setSpellCastTimingRestriction(SpellCastTimingRestriction.COMBAT_AFTER_BLOCKERS);

        // Flip a coin. If you win the flip, target creature gets +1/+1 until end of turn.
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new FlipCoinWinEffect(new BoostTargetCreatureEffect(1, 1)));

        // Draw a card at the beginning of the next turn's upkeep.
        addEffect(EffectSlot.SPELL, new RegisterDrawCardsAtNextUpkeepEffect());
    }
}
