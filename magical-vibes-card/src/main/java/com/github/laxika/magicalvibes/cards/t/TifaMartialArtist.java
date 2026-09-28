package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.FirstCombatPhase;
import com.github.laxika.magicalvibes.model.effect.AdditionalCombatPhaseEffect;
import com.github.laxika.magicalvibes.model.effect.AllyCombatDamageTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate;

@CardRegistration(set = "FIC", collectorNumber = "6")
@CardRegistration(set = "FIC", collectorNumber = "188")
@CardRegistration(set = "FIC", collectorNumber = "206")
@CardRegistration(set = "FIC", collectorNumber = "214")
@CardRegistration(set = "FIC", collectorNumber = "225")
public class TifaMartialArtist extends Card {

    public TifaMartialArtist() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER,
                new AllyCombatDamageTriggerEffect(
                        new PermanentPowerAtLeastPredicate(7),
                        SequenceEffect.of(
                                new UntapPermanentsEffect(TapUntapScope.CONTROLLED,
                                        new PermanentIsCreaturePredicate()),
                                new ConditionalEffect(new FirstCombatPhase(),
                                        new AdditionalCombatPhaseEffect(1))),
                        false,
                        true));
    }
}
