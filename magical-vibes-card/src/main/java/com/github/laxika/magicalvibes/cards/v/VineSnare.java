package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate;

@CardRegistration(set = "ORI", collectorNumber = "205")
public class VineSnare extends Card {

    public VineSnare() {
        // Prevent all combat damage that would be dealt this turn by creatures with power 4 or less.
        // The exemption predicate is the complement: creatures with power 5 or greater still deal damage.
        addEffect(EffectSlot.SPELL, PreventDamageEffect.allCombatExcept(new PermanentPowerAtLeastPredicate(5)));
    }
}
