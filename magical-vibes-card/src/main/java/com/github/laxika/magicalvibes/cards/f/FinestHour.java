package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.AttacksAlone;
import com.github.laxika.magicalvibes.model.condition.FirstCombatPhase;
import com.github.laxika.magicalvibes.model.effect.AdditionalCombatPhaseEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;

@CardRegistration(set = "ARB", collectorNumber = "126")
public class FinestHour extends Card {

    public FinestHour() {
        // Exalted is auto-loaded from the card's keywords. Second ability: whenever a creature you
        // control attacks alone, if it's the first combat phase of the turn, untap that creature and,
        // after this phase, add an additional combat phase. The AttacksAlone wrapper is stripped at
        // ON_ALLY_CREATURE_ATTACKS collection (so the ability only fires when attacking alone), leaving
        // the FirstCombatPhase intervening-"if" to be re-checked at resolution — that gate stops the
        // extra combat phase it creates from looping. "That creature" is the sole attacker, so the
        // attacked-creatures untap targets exactly it.
        addEffect(EffectSlot.ON_ALLY_CREATURE_ATTACKS, new ConditionalEffect(new AttacksAlone(),
                new ConditionalEffect(new FirstCombatPhase(),
                        new UntapPermanentsEffect(TapUntapScope.ATTACKED_CREATURES))));
        addEffect(EffectSlot.ON_ALLY_CREATURE_ATTACKS, new ConditionalEffect(new AttacksAlone(),
                new ConditionalEffect(new FirstCombatPhase(),
                        new AdditionalCombatPhaseEffect(1))));
    }
}
