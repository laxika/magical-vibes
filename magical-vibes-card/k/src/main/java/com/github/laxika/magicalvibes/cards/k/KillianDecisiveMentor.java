package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GoadTargetCreatureUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.condition.MinimumMatchingAttackers;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantedBySourceControllerAuraPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SOC", collectorNumber = "4")
public class KillianDecisiveMentor extends Card {

    public KillianDecisiveMentor() {
        target(TargetFilters.creature(), 0, 1).addTrigger(Triggers.enters(Triggers.ENCHANTMENT,
                ObjectRelation.ANOTHER_YOU_CONTROL),
                SequenceEffect.of(
                        new TapPermanentsEffect(TapUntapScope.TARGET),
                        new GoadTargetCreatureUntilNextTurnEffect()));

        addTrigger(Triggers.playerAttacks(),
                TriggerOptions.DEFAULT.withInterveningIf(
                        new MinimumMatchingAttackers(1, new PermanentIsEnchantedBySourceControllerAuraPredicate())),
                new DrawCardEffect());
    }
}
