package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.amount.CountersOnStackEntryCard;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.ChooseNewTargetsForTargetSpellEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnStackEntryCardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "CSP", collectorNumber = "89")
public class LightningStorm extends Card {

    public LightningStorm() {
        addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(
                new Sum(new Fixed(3), new CountersOnStackEntryCard(CounterType.CHARGE))));
        addStackActivatedAbility(new ActivatedAbility(false, null, List.of(
                new DiscardCardTypeCost(new CardTypePredicate(CardType.LAND), "land"),
                new PutCountersOnStackEntryCardEffect(CounterType.CHARGE, 2),
                new MayEffect(new ChooseNewTargetsForTargetSpellEffect(), "Choose new targets?")),
                "Discard a land card: Put two charge counters on Lightning Storm. You may choose a new target for it.")
                .withActivatableByAnyPlayer());
    }
}
