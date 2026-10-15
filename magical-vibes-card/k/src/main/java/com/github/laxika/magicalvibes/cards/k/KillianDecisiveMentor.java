package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GoadTargetCreatureUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.condition.MinimumMatchingAttackers;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantedBySourceControllerAuraPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "SOC", collectorNumber = "4")
public class KillianDecisiveMentor extends Card {

    public KillianDecisiveMentor() {
        target(TargetFilters.creature(), 0, 1).addEffect(
                EffectSlot.ON_ALLY_ENCHANTMENT_ENTERS_BATTLEFIELD,
                SequenceEffect.of(
                        new TapPermanentsEffect(TapUntapScope.TARGET),
                        new GoadTargetCreatureUntilNextTurnEffect()));

        addEffect(EffectSlot.ON_ANY_PLAYER_ATTACKS,
                new ConditionalEffect(
                        new MinimumMatchingAttackers(1,
                                new PermanentIsEnchantedBySourceControllerAuraPredicate()),
                        new DrawCardEffect()));
    }
}
