package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.SpellCastTimingRestriction;
import com.github.laxika.magicalvibes.model.effect.PayXLifeDrawXCardsEffect;

@CardRegistration(set = "7ED", collectorNumber = "149")
public class Necrologia extends Card {

    public Necrologia() {
        // Cast this spell only during your end step.
        setSpellCastTimingRestriction(SpellCastTimingRestriction.YOUR_END_STEP);

        // As an additional cost to cast this spell, pay X life. Draw X cards.
        // Modeled as a single resolution-time X choice: pay X life, draw X cards.
        addEffect(EffectSlot.SPELL, new PayXLifeDrawXCardsEffect());
    }
}
