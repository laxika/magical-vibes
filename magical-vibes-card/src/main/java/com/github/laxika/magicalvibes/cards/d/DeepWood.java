package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.SpellCastTimingRestriction;
import com.github.laxika.magicalvibes.model.effect.PreventAllDamageToControllerFromAttackingCreaturesEffect;

@CardRegistration(set = "POR", collectorNumber = "162")
public class DeepWood extends Card {

    public DeepWood() {
        // Cast only during the declare attackers step and only if you've been attacked this step.
        setSpellCastTimingRestriction(SpellCastTimingRestriction.DECLARE_ATTACKERS_IF_ATTACKED);

        // Prevent all damage that would be dealt to you this turn by attacking creatures.
        addEffect(EffectSlot.SPELL, new PreventAllDamageToControllerFromAttackingCreaturesEffect());
    }
}
