package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CascadeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantCastTriggerToSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerGrant;

@CardRegistration(set = "ARB", collectorNumber = "130")
@CardRegistration(set = "2XM", collectorNumber = "206")
@CardRegistration(set = "DMC", collectorNumber = "159")
public class MaelstromNexus extends Card {

    public MaelstromNexus() {
        // The first spell you cast each turn has cascade. TriggerCollectionService.checkSpellCastTriggers
        // queues the CascadeEffect keyed to the caster's first spell of the turn (CascadeEffectHandler resolves it).
        addStaticEffect(new GrantCastTriggerToSpellsEffect(
                SpellCastTriggerGrant.CASCADE_TO_FIRST_SPELL, new CascadeEffect()));
    }
}
