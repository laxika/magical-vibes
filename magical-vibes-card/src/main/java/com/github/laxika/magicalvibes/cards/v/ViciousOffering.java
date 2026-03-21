package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.effect.KickerReplacementEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "DOM", collectorNumber = "110")
public class ViciousOffering extends Card {

    public ViciousOffering() {
        // Kicker—Sacrifice a creature.
        addEffect(EffectSlot.STATIC, new KickerEffect(
                new PermanentIsCreaturePredicate(),
                "a creature"
        ));
        // Target creature gets -2/-2 until end of turn.
        // If this spell was kicked, that creature gets -5/-5 until end of turn instead.
        addEffect(EffectSlot.SPELL, new KickerReplacementEffect(
                new BoostTargetCreatureEffect(-2, -2),
                new BoostTargetCreatureEffect(-5, -5)
        ));
    }
}
