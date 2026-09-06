package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DelayedTargetGroup;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsMayExileOneAndPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedBeginningOfCombatTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.TargetDealsPowerDamageToTargetEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "WOE", collectorNumber = "169")
public class FeralEncounter extends Card {

    public FeralEncounter() {
        addEffect(EffectSlot.SPELL, new LookAtTopCardsMayExileOneAndPlayThisTurnEffect(
                5, new CardTypePredicate(CardType.CREATURE), false));

        addEffect(EffectSlot.SPELL, new RegisterDelayedBeginningOfCombatTriggerEffect(
                List.of(
                        new DelayedTargetGroup(TargetFilters.creatureYouControl(), 1, 1),
                        new DelayedTargetGroup(TargetFilters.creatureAnOpponentControls(), 0, 1)),
                new TargetDealsPowerDamageToTargetEffect()));
    }
}
