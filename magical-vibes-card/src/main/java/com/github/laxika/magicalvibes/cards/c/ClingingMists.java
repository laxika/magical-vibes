package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ControllerLifeAtOrBelowThresholdConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PreventAllCombatDamageEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapAllAttackingCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.TapAllAttackingCreaturesEffect;

@CardRegistration(set = "DKA", collectorNumber = "109")
public class ClingingMists extends Card {

    public ClingingMists() {
        // Prevent all combat damage that would be dealt this turn.
        addEffect(EffectSlot.SPELL, new PreventAllCombatDamageEffect());

        // Fateful hour — If you have 5 or less life, tap all attacking creatures.
        // Those creatures don't untap during their controller's next untap step.
        addEffect(EffectSlot.SPELL, new ControllerLifeAtOrBelowThresholdConditionalEffect(5,
                new TapAllAttackingCreaturesEffect()));
        addEffect(EffectSlot.SPELL, new ControllerLifeAtOrBelowThresholdConditionalEffect(5,
                new SkipNextUntapAllAttackingCreaturesEffect()));
    }
}
