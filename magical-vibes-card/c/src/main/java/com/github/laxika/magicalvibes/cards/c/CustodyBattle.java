package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerGainsControlOfSourceCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "ONS", collectorNumber = "197")
public class CustodyBattle extends Card {

    public CustodyBattle() {
        target(TargetFilters.creature())
                // Enchanted creature has "At the beginning of your upkeep, target opponent gains control of this
                // creature unless you sacrifice a land."
                .addStaticEffect(new GrantTriggeredAbilityEffect(new TriggeredAbility(
                        Triggers.upkeep(ActivePlayer.YOU),
                        new ForcedCostOrElseEffect(
                                new SacrificePermanentCost(new PermanentIsLandPredicate(), "Sacrifice a land"),
                                List.of(new TargetPlayerGainsControlOfSourceCreatureEffect()),
                                true))
                        .withTargetFilter(new PlayerPredicateTargetFilter(
                                new PlayerRelationPredicate(PlayerRelation.OPPONENT), "Target must be an opponent")),
                        GrantScope.ENCHANTED_CREATURE));
    }
}
