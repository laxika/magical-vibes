package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.filter.AnyTargetPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsBattlePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "MOM", collectorNumber = "90")
public class AyaraWidowOfTheRealm extends Card {

    public AyaraWidowOfTheRealm() {
        setBackFaceCard(new AyaraFurnaceQueen());

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new SacrificePermanentCost(
                                new PermanentAnyOfPredicate(List.of(
                                        new PermanentIsCreaturePredicate(),
                                        new PermanentIsArtifactPredicate())),
                                "another creature or artifact", true, false, true, false),
                        new DealDamageToAnyTargetEffect(new XValue()),
                        new GainLifeEffect(new XValue())),
                "{T}, Sacrifice another creature or artifact: Ayara deals X damage to target opponent or battle and you gain X life, where X is the sacrificed permanent's mana value.",
                new AnyTargetPredicateTargetFilter(
                        new PermanentIsBattlePredicate(),
                        new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                        "Target must be an opponent or battle")));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{5}{R/P}",
                List.of(new TransformSelfEffect()),
                "{5}{R/P}: Transform Ayara. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED));
    }

    @Override
    public String getBackFaceClassName() {
        return "AyaraFurnaceQueen";
    }
}
