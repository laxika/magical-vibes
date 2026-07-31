package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "ALL", collectorNumber = "106")
public class EnergyArc extends Card {

    public EnergyArc() {
        // "Untap any number of target creatures. Prevent all combat damage that would be dealt to and
        // dealt by those creatures this turn." One target group; both prevention effects are combat-only
        // and fan over every chosen target.
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Targets must be creatures"
        ), 0, 99).addEffect(EffectSlot.SPELL, new UntapPermanentsEffect(TapUntapScope.ALL_TARGETS))
                .addEffect(EffectSlot.SPELL, PreventDamageEffect.allCombatToTargetCreatures())
                .addEffect(EffectSlot.SPELL, PreventDamageEffect.allCombatByTargetCreatures());
    }
}
