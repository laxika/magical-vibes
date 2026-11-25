package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.RemoveTargetFromCombatEffect;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsHostOfSourceAuraPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.AbilityTrigger;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "LEG", collectorNumber = "107")
public class Imprison extends Card {

    public Imprison() {
        target(TargetFilters.creature());
        CardEffect activationTrigger = UnlessPaysEffect.youMayPay("{1}", new CounterSpellEffect())
                .ifNotPaid(new DestroyTargetPermanentEffect(Subjects.source()))
                .withPrompt("Pay {1} to counter that ability?");
        addTrigger(Triggers.youActivateNonManaAbility()
                        .withKind(AbilityTrigger.AbilityKind.WITH_TAP_COST)
                        .withSource(new PermanentIsHostOfSourceAuraPredicate()),
                activationTrigger);
        addTrigger(Triggers.opponentActivatesNonManaAbility()
                        .withKind(AbilityTrigger.AbilityKind.WITH_TAP_COST)
                        .withSource(new PermanentIsHostOfSourceAuraPredicate()),
                activationTrigger);

        CardEffect combatTrigger = UnlessPaysEffect.youMayPay("{1}", SequenceEffect.of(
                        new TapPermanentsEffect(Subjects.attached()),
                        new RemoveTargetFromCombatEffect(Subjects.triggering(), true))).ifNotPaid(new DestroyTargetPermanentEffect(Subjects.source())).withPrompt("Pay {1} to tap and remove the creature from combat?");
        addTrigger(List.of(Triggers.attacks(), Triggers.blocks()), TriggerOptions.ONE_OR_MORE, combatTrigger);
    }
}
