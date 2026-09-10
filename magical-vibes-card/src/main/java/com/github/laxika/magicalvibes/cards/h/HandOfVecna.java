package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.condition.EnchantedPermanentMatches;
import com.github.laxika.magicalvibes.model.effect.BoostEquippedCreatureUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EquipEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.PayLifeForEachCardInHandCost;
import com.github.laxika.magicalvibes.model.filter.PermanentNamedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "AFR", collectorNumber = "246")
public class HandOfVecna extends Card {

    public HandOfVecna() {
        CardsInHand hand = new CardsInHand(CountScope.CONTROLLER);
        PermanentNamedPredicate vecna = new PermanentNamedPredicate("Vecna");

        addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED, ConditionalEffect.unless(
                new EnchantedPermanentMatches(
                        new PermanentNotPredicate(vecna), "equipped creature isn't named Vecna"),
                new BoostEquippedCreatureUntilEndOfTurnEffect(hand, hand)));
        addEffect(EffectSlot.STATIC, new GrantTriggeredAbilityEffect(
                EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED,
                new BoostSelfEffect(hand, hand),
                GrantScope.ALL_OWN_CREATURES,
                vecna));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new PayLifeForEachCardInHandCost(), new EquipEffect()),
                "Equip—Pay 1 life for each card in your hand.",
                TargetFilters.creatureYouControl(),
                null,
                null,
                ActivationTimingRestriction.SORCERY_SPEED
        ));
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
