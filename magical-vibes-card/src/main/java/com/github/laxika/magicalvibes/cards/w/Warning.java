package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "ICE", collectorNumber = "55")
public class Warning extends Card {

    public Warning() {
        // Prevent all combat damage that would be dealt by target attacking creature this turn.
        target(new PermanentPredicateTargetFilter(
                new PermanentIsAttackingPredicate(),
                "Target must be an attacking creature"
        )).addEffect(EffectSlot.SPELL, PreventDamageEffect.allCombatByTargetCreatures());
    }
}
