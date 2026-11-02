package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.AttacksAlone;
import com.github.laxika.magicalvibes.model.condition.FirstCombatPhase;
import com.github.laxika.magicalvibes.model.effect.AdditionalCombatPhaseEffect;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ARB", collectorNumber = "126")
@CardRegistration(set = "C18", collectorNumber = "180")
public class FinestHour extends Card {

    public FinestHour() {
        // Exalted: Triggers.creatureYouControlAttacks() fires per attacking ally and records the attacker as the
        // trigger's (non-targeting) target, so BoostTargetCreatureEffect boosts "that creature";
        // AttacksAlone (checked at resolution) restricts it to lone attackers.
        addTrigger(Triggers.creatureYouControlAttacks(), new ConditionalEffect(new AttacksAlone(),
                new BoostTargetCreatureEffect(1, 1)));
        // Second ability: whenever a creature you control attacks alone, if it's the first combat phase
        // of the turn, untap that creature and, after this phase, add an additional combat phase. The
        // AttacksAlone wrapper is stripped at Triggers.creatureYouControlAttacks() collection (so the ability only
        // fires when attacking alone), leaving the FirstCombatPhase intervening-"if" to be re-checked at
        // resolution — that gate stops the extra combat phase it creates from looping. "That creature"
        // is the sole attacker, so the attacked-creatures untap targets exactly it.
        addTrigger(Triggers.creatureYouControlAttacks(), new ConditionalEffect(new AttacksAlone(),
                new ConditionalEffect(new FirstCombatPhase(),
                        new UntapPermanentsEffect(TapUntapScope.ATTACKED_CREATURES))));
        addTrigger(Triggers.creatureYouControlAttacks(), new ConditionalEffect(new AttacksAlone(),
                new ConditionalEffect(new FirstCombatPhase(),
                        new AdditionalCombatPhaseEffect(1))));
    }
}
