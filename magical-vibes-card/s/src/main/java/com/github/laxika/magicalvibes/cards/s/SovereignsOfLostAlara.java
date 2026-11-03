package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.AttacksAlone;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForAuraToBattlefieldAttachedToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ARB", collectorNumber = "12")
@CardRegistration(set = "UMA", collectorNumber = "207")
public class SovereignsOfLostAlara extends Card {

    public SovereignsOfLostAlara() {
        // Exalted: whenever a creature you control attacks alone, that creature gets +1/+1 until end
        // of turn. Same wiring as Battlegrace Angel — Triggers.creatureYouControlAttacks() records the lone
        // attacker as the non-targeting trigger target and AttacksAlone restricts it to solo attackers.
        addTrigger(Triggers.creatureYouControlAttacks(),
                TriggerOptions.DEFAULT.withInterveningIf(new AttacksAlone()),
                new BoostTargetCreatureEffect(1, 1));

        // Whenever a creature you control attacks alone, you may search your library for an Aura card
        // that could enchant that creature and put it onto the battlefield attached to it. The search
        // reads the recorded lone attacker (the trigger's non-targeting target) as the host.
        addTrigger(Triggers.creatureYouControlAttacks(),
                TriggerOptions.DEFAULT.withInterveningIf(new AttacksAlone()),
                new MayEffect(new SearchLibraryForAuraToBattlefieldAttachedToTargetCreatureEffect(),
                        "Search your library for an Aura card to attach to the attacking creature?"));
    }
}
