package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveKeywordEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MIR", collectorNumber = "207")
public class BarbedFoliage extends Card {

    public BarbedFoliage() {
        // Whenever a creature attacks you, it loses flanking until end of turn.
        // The trigger's non-targeting targetId is the attacking creature, so the target() subject
        // strips flanking from it without any target selection.
        addTrigger(Triggers.creatureAttacksYou(),
                new RemoveKeywordEffect(Keyword.FLANKING, Subjects.target()));

        // Whenever a creature without flying attacks you, this enchantment deals 1 damage to it.
        PermanentPredicate withoutFlying = new PermanentNotPredicate(new PermanentHasKeywordPredicate(Keyword.FLYING));
        addTrigger(Triggers.creatureAttacksYou().withFilter(withoutFlying),
                new DealDamageToTargetCreatureEffect(Subjects.triggering(), 1));
    }
}
