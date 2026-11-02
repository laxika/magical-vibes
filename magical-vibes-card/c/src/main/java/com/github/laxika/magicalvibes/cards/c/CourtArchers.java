package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.AttacksAlone;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ALA", collectorNumber = "126")
public class CourtArchers extends Card {

    public CourtArchers() {
        // Reach is auto-loaded from Scryfall keywords; only Exalted needs engine logic.
        // Exalted: whenever a creature you control attacks alone, that creature gets +1/+1 until
        // end of turn. Triggers.creatureYouControlAttacks() fires per attacking ally and records the attacker
        // as the trigger's (non-targeting) target, so BoostTargetCreatureEffect boosts "that
        // creature"; AttacksAlone (checked at resolution) restricts it to lone attackers.
        addTrigger(Triggers.creatureYouControlAttacks(),
                new ConditionalEffect(new AttacksAlone(), new BoostTargetCreatureEffect(1, 1)));
    }
}
