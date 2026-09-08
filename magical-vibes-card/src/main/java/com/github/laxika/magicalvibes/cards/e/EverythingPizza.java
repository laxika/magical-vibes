package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.SpellTarget;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerGainsLifeEffect;
import com.github.laxika.magicalvibes.model.filter.AnyTargetPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsBattlePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "TMT", collectorNumber = "173")
public class EverythingPizza extends Card {

    public EverythingPizza() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new SearchLibraryEffect(CardPredicateUtils.basicLand()));

        TargetFilter targetPlayer = new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY), "Target must be a player");
        TargetFilter anyTarget = new AnyTargetPredicateTargetFilter(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsPlaneswalkerPredicate(),
                        new PermanentIsBattlePredicate())),
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a creature, planeswalker, battle, or player");
        TargetFilter targetCreatureYouControl = TargetFilters.creatureYouControl();

        SpellTarget playerTarget = target(targetPlayer);
        SpellTarget damageTarget = target(anyTarget);
        SpellTarget counterTarget = target(targetCreatureYouControl, 0, 1);

        TargetPlayerGainsLifeEffect gainLife = new TargetPlayerGainsLifeEffect(3);
        DrawCardForTargetPlayerEffect draw = new DrawCardForTargetPlayerEffect(1, false, true);
        DealDamageToAnyTargetEffect damage = DealDamageToAnyTargetEffect.forTargetGroup(3,
                damageTarget.getIndex());
        PutCounterOnTargetPermanentEffect counters = new PutCounterOnTargetPermanentEffect(
                CounterType.PLUS_ONE_PLUS_ONE, 3);
        registerEffectTargetIndex(gainLife, playerTarget.getIndex());
        registerEffectTargetIndex(draw, playerTarget.getIndex());
        registerEffectTargetIndex(damage, damageTarget.getIndex());
        registerEffectTargetIndex(counters, counterTarget.getIndex());

        ActivatedAbility ability = new ActivatedAbility(
                true,
                "{2}{W}{U}{B}{R}{G}",
                List.of(
                        new SacrificeSelfCost(),
                        gainLife,
                        draw,
                        new DiscardEffect(1, DiscardRecipient.EACH_OPPONENT),
                        damage,
                        counters),
                "{2}{W}{U}{B}{R}{G}, {T}, Sacrifice this artifact: Target player gains 3 life and draws a card. "
                        + "Each of your opponents discards a card. This artifact deals 3 damage to any target. "
                        + "Put three +1/+1 counters on up to one target creature.",
                null, null, null, null,
                List.of(targetPlayer, anyTarget, targetCreatureYouControl), 2, 3);
        addActivatedAbility(ability.withAllowSharedTargets());
    }
}
