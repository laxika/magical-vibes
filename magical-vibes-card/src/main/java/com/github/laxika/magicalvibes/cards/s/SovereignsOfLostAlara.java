package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.AttacksAlone;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForAuraToBattlefieldAttachedToTargetCreatureEffect;

@CardRegistration(set = "ARB", collectorNumber = "12")
public class SovereignsOfLostAlara extends Card {

    public SovereignsOfLostAlara() {
        // Exalted: whenever a creature you control attacks alone, that creature gets +1/+1 until end
        // of turn. Same wiring as Battlegrace Angel — ON_ALLY_CREATURE_ATTACKS records the lone
        // attacker as the non-targeting trigger target and AttacksAlone restricts it to solo attackers.
        addEffect(EffectSlot.ON_ALLY_CREATURE_ATTACKS,
                new ConditionalEffect(new AttacksAlone(), new BoostTargetCreatureEffect(1, 1)));

        // Whenever a creature you control attacks alone, you may search your library for an Aura card
        // that could enchant that creature and put it onto the battlefield attached to it. The search
        // reads the recorded lone attacker (the trigger's non-targeting target) as the host.
        addEffect(EffectSlot.ON_ALLY_CREATURE_ATTACKS,
                new ConditionalEffect(new AttacksAlone(),
                        new MayEffect(new SearchLibraryForAuraToBattlefieldAttachedToTargetCreatureEffect(),
                                "Search your library for an Aura card to attach to the attacking creature?")));
    }
}
