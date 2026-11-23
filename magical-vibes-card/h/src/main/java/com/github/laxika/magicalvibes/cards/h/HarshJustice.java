package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.SpellCastTimingRestriction;
import com.github.laxika.magicalvibes.model.effect.RegisterGlobalTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;

@CardRegistration(set = "POR", collectorNumber = "18")
@CardRegistration(set = "ME4", collectorNumber = "13")
public class HarshJustice extends Card {

    public HarshJustice() {
        // Cast only during the declare attackers step and only if you've been attacked this step.
        setSpellCastTimingRestriction(SpellCastTimingRestriction.DECLARE_ATTACKERS_IF_ATTACKED);

        // This turn, whenever an attacking creature deals combat damage to you, it deals that much
        // damage to its controller.
        addSpellEffect(new RegisterGlobalTriggeredAbilityEffect(new TriggeredAbility(
                Triggers.creatureDealsCombatDamageToYou().withFilter(new PermanentIsAttackingPredicate()),
                new DealDamageToPlayersEffect(new EventValue(), Subjects.controllerOf(Subjects.triggering()))
                        .dealtByEventDamageSource())));
    }
}
