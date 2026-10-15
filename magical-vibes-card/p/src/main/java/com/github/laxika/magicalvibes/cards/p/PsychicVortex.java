package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CumulativeUpkeepEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardHandEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

/**
 * Psychic Vortex — enchantment whose cumulative upkeep is drawing cards, paid for by sacrificing a
 * land and emptying your hand every end step.
 */
@CardRegistration(set = "WTH", collectorNumber = "50")
public class PsychicVortex extends Card {

    public PsychicVortex() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, CumulativeUpkeepEffect.drawCard());
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, SequenceEffect.of(
                new SacrificePermanentsEffect(1, new PermanentIsLandPredicate(), SacrificeRecipient.CONTROLLER),
                new DiscardHandEffect()));
    }
}
