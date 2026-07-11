package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerLifeAtMost;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PreventAllCombatDamageEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;

@CardRegistration(set = "DKA", collectorNumber = "109")
public class ClingingMists extends Card {

    public ClingingMists() {
        // Prevent all combat damage that would be dealt this turn.
        addEffect(EffectSlot.SPELL, new PreventAllCombatDamageEffect());

        // Fateful hour — If you have 5 or less life, tap all attacking creatures.
        // Those creatures don't untap during their controller's next untap step.
        addEffect(EffectSlot.SPELL, new ConditionalEffect(new ControllerLifeAtMost(5), new TapPermanentsEffect(TapUntapScope.ALL_CREATURES, new PermanentIsAttackingPredicate())));
        addEffect(EffectSlot.SPELL, new ConditionalEffect(new ControllerLifeAtMost(5), new SkipNextUntapEffect(TapUntapScope.ALL_CREATURES, new PermanentIsAttackingPredicate())));
    }
}
