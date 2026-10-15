package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.SpellCastTimingRestriction;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;

@CardRegistration(set = "POR", collectorNumber = "12")
public class DefiantStand extends Card {

    public DefiantStand() {
        // Cast only during the declare attackers step and only if you've been attacked this step.
        setSpellCastTimingRestriction(SpellCastTimingRestriction.DECLARE_ATTACKERS_IF_ATTACKED);

        // Target creature gets +1/+3 until end of turn. Untap that creature (same target group).
        addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(1, 3));
        addEffect(EffectSlot.SPELL, new UntapPermanentsEffect(TapUntapScope.TARGET));
    }
}
