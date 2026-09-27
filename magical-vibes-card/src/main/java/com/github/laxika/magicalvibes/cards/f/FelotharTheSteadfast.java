package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AssignCombatDamageWithToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MatchingCreaturesCanAttackAsThoughNoDefenderEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.amount.SacrificedPermanentPower;
import com.github.laxika.magicalvibes.model.amount.SacrificedPermanentToughness;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "TDC", collectorNumber = "4")
public class FelotharTheSteadfast extends Card {

    public FelotharTheSteadfast() {
        addEffect(EffectSlot.STATIC,
                new AssignCombatDamageWithToughnessEffect(GrantScope.ALL_OWN_CREATURES));
        addEffect(EffectSlot.STATIC,
                new MatchingCreaturesCanAttackAsThoughNoDefenderEffect(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentControlledBySourceControllerPredicate(),
                                new PermanentIsCreaturePredicate()))));
        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}",
                List.of(
                        new SacrificeCreatureCost(false, true, true, true),
                        new DrawCardEffect(new SacrificedPermanentToughness()),
                        new DiscardEffect(new SacrificedPermanentPower(), DiscardRecipient.CONTROLLER)
                ),
                "{3}, {T}, Sacrifice another creature: Draw cards equal to the sacrificed creature's toughness, then discard cards equal to its power."
        ));
    }
}
