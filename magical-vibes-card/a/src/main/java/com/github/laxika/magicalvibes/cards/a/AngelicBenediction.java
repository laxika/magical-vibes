package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.AttacksAlone;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ALA", collectorNumber = "3")
@CardRegistration(set = "M13", collectorNumber = "4")
@CardRegistration(set = "DDC", collectorNumber = "19")
@CardRegistration(set = "DVD", collectorNumber = "19")
public class AngelicBenediction extends Card {

    public AngelicBenediction() {
        // Exalted: whenever a creature you control attacks alone, that creature gets +1/+1 until end
        // of turn. Same wiring as Akrasan Squire — Triggers.creatureYouControlAttacks() records the lone attacker
        // as the non-targeting trigger target and AttacksAlone restricts it to solo attackers.
        addTrigger(Triggers.creatureYouControlAttacks(),
                new ConditionalEffect(new AttacksAlone(), new BoostTargetCreatureEffect(1, 1)));

        // Whenever a creature you control attacks alone, you may tap target creature. The optional tap
        // resolves through the may-ability flow, which chooses the target using this card's filter.
        target(TargetFilters.creature()).addTrigger(Triggers.creatureYouControlAttacks(),
                new ConditionalEffect(new AttacksAlone(),
                        new MayEffect(new TapPermanentsEffect(TapUntapScope.TARGET),
                                "Tap target creature?")));
    }
}
