package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.EnchantedPermanentMatches;
import com.github.laxika.magicalvibes.model.effect.AdditionalCombatPhaseEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeEnchantedPermanentAndReattachSourceAuraEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "RAV", collectorNumber = "116")
public class BreathOfFury extends Card {

    public BreathOfFury() {
        target(TargetFilters.creatureYouControl());
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                SequenceEffect.of(
                        new SacrificeEnchantedPermanentAndReattachSourceAuraEffect(
                                new PermanentIsCreaturePredicate()),
                        new ConditionalEffect(
                                new EnchantedPermanentMatches(new PermanentIsCreaturePredicate(),
                                        "Breath of Fury is attached to a creature"),
                                SequenceEffect.of(
                                        new UntapPermanentsEffect(TapUntapScope.CONTROLLED,
                                                new PermanentIsCreaturePredicate()),
                                        new AdditionalCombatPhaseEffect(1)))));
    }
}
