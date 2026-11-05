package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.AttacksAlone;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M13", collectorNumber = "103")
public class NefaroxOverlordOfGrixis extends Card {

    public NefaroxOverlordOfGrixis() {
        // Flying is auto-loaded from Scryfall keywords. Exalted: whenever a creature you control
        // attacks alone, that creature gets +1/+1 until end of turn. Triggers.creatureYouControlAttacks()
        // records the attacker as the non-targeting trigger target and AttacksAlone restricts it
        // to lone attackers.
        addTrigger(Triggers.creatureYouControlAttacks(),
                TriggerOptions.DEFAULT.withEventCondition(new AttacksAlone()),
                new BoostTargetCreatureEffect(1, 1));

        // Whenever Nefarox attacks alone, defending player sacrifices a creature of their choice.
        addTrigger(Triggers.attacks(),
                TriggerOptions.DEFAULT.withEventCondition(new AttacksAlone()),
                new SacrificePermanentsEffect(
                        1, new PermanentIsCreaturePredicate(), SacrificeRecipient.DEFENDING_PLAYER));
    }
}
