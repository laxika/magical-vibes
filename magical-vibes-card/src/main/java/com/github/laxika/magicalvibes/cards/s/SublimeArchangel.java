package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.condition.AttacksAlone;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "M13", collectorNumber = "36")
public class SublimeArchangel extends Card {

    public SublimeArchangel() {
        // Flying and exalted are auto-loaded keywords. Sublime Archangel's own exalted plus the
        // exalted it grants to every other creature you control add up to one instance per
        // creature you control (its own, the lone attacker's granted one, and one for each other
        // creature), so a lone attacker gets +N/+N where N is the number of creatures you control
        // — matching the official ruling (three creatures out ⇒ +3/+3). Modelled as a single
        // ON_ALLY_CREATURE_ATTACKS trigger that boosts the recorded lone attacker by that count;
        // AttacksAlone restricts it to lone attackers (CR 702.83a).
        addEffect(EffectSlot.ON_ALLY_CREATURE_ATTACKS,
                new ConditionalEffect(new AttacksAlone(), new BoostTargetCreatureEffect(
                        new PermanentCount(new PermanentIsCreaturePredicate(), CountScope.CONTROLLER),
                        new PermanentCount(new PermanentIsCreaturePredicate(), CountScope.CONTROLLER))));
    }
}
