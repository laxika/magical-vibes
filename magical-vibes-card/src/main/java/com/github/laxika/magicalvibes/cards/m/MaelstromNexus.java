package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CascadeEffect;

@CardRegistration(set = "ARB", collectorNumber = "130")
public class MaelstromNexus extends Card {

    public MaelstromNexus() {
        // The first spell you cast each turn has cascade. TriggerCollectionService.checkSpellCastTriggers
        // detects this slot by presence and, on the caster's first spell of the turn, queues the
        // CascadeEffect keyed to that spell (CascadeEffectHandler resolves it).
        addEffect(EffectSlot.GRANT_CASCADE_TO_FIRST_SPELL, new CascadeEffect());
    }
}
