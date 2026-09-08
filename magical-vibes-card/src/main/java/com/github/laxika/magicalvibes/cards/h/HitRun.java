package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerSacrificesPermanentThenDealsManaValueDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import java.util.List;

@CardRegistration(set = "DIS", collectorNumber = "152")
public class HitRun extends Card {

    public HitRun() {
        PermanentPredicate artifactOrCreature = new PermanentAnyOfPredicate(List.of(
                new PermanentIsArtifactPredicate(),
                new PermanentIsCreaturePredicate()));
        PlayerPredicateTargetFilter anyPlayer = new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY), "Target must be a player");
        PermanentIsAttackingPredicate attacking = new PermanentIsAttackingPredicate();

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Hit — Target player sacrifices an artifact or creature of their choice. Hit deals damage to that player equal to that permanent's mana value.",
                        new TargetPlayerSacrificesPermanentThenDealsManaValueDamageEffect(artifactOrCreature),
                        anyPlayer).withManaCost("{1}{B}{R}"),
                new ChooseOneEffect.ChooseOneOption(
                        "Run — Attacking creatures you control get +1/+0 until end of turn for each other attacking creature.",
                        new BoostAllOwnCreaturesEffect(
                                new Sum(new PermanentCount(attacking, CountScope.CONTROLLER), new Fixed(-1)),
                                new Fixed(0), attacking)).withManaCost("{3}{R}{G}")
        )));
    }
}
