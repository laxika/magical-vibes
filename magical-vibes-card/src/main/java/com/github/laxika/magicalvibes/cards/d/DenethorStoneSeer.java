package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.SpellTarget;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerBecomesMonarchEffect;
import com.github.laxika.magicalvibes.model.filter.AnyTargetPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsBattlePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;

import java.util.List;

@CardRegistration(set = "LTC", collectorNumber = "20")
@CardRegistration(set = "LTC", collectorNumber = "103")
public class DenethorStoneSeer extends Card {

    public DenethorStoneSeer() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ScryEffect(2));

        TargetFilter targetPlayer = new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY), "Target must be a player");
        TargetFilter anyTarget = new AnyTargetPredicateTargetFilter(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsPlaneswalkerPredicate(),
                        new PermanentIsBattlePredicate())),
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a creature, planeswalker, battle, or player");

        SpellTarget monarchTarget = target(targetPlayer);
        SpellTarget damageTarget = target(anyTarget);
        TargetPlayerBecomesMonarchEffect monarchEffect =
                TargetPlayerBecomesMonarchEffect.forTargetGroup(monarchTarget.getIndex());
        DealDamageToAnyTargetEffect damageEffect =
                DealDamageToAnyTargetEffect.forTargetGroup(3, damageTarget.getIndex());

        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}{R}",
                List.of(new SacrificeSelfCost(), monarchEffect, damageEffect),
                "{3}{R}, {T}, Sacrifice Denethor: Target player becomes the monarch. "
                        + "Denethor deals 3 damage to any target.",
                null,
                null,
                null,
                null,
                List.of(targetPlayer, anyTarget),
                2,
                2
        ));
    }
}
