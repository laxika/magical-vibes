package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.AttacksAlone;
import com.github.laxika.magicalvibes.model.condition.FirstCombatPhase;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.AdditionalCombatPhaseEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAttackedThisTurnPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import java.util.List;

@CardRegistration(set = "ARB", collectorNumber = "126")
@CardRegistration(set = "C18", collectorNumber = "180")
public class FinestHour extends Card {

    public FinestHour() {
        // Exalted (CR 702.83a): Triggers.creatureYouControlAttacks() fires per attacking ally and records the attacker
        // as the trigger's (non-targeting) target, so BoostEffect.target boosts "that creature"; "attacks
        // alone" is part of the trigger event, so it is the event condition.
        addTrigger(Triggers.creatureYouControlAttacks(),
                TriggerOptions.DEFAULT.withEventCondition(new AttacksAlone()),
                BoostEffect.target(1, 1));
        // Second ability: whenever a creature you control attacks alone, if it's the first combat phase of the turn,
        // untap that creature and, after this phase, add an additional combat phase. "If it's the first combat
        // phase" is an intervening "if" (CR 603.4), checked as the ability triggers and again as it resolves, so the
        // extra combat phase it creates doesn't loop. "That creature" is the sole attacker, so the attacked-creatures
        // untap targets exactly it.
        addTrigger(Triggers.creatureYouControlAttacks(),
                TriggerOptions.DEFAULT.withEventCondition(new AttacksAlone())
                        .withInterveningIf(new FirstCombatPhase()),
                new UntapPermanentsEffect(Subjects.controlledBy(Subjects.eachPlayer(),
                        new PermanentAllOfPredicate(List.of(new PermanentIsCreaturePredicate(),
                                new PermanentAttackedThisTurnPredicate())))),
                new AdditionalCombatPhaseEffect(1));
    }
}
