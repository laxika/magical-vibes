package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNamedPredicate;

import java.util.List;

@CardRegistration(set = "WOE", collectorNumber = "129")
public class FoodFight extends Card {

    public FoodFight() {
        PermanentCount foodFightsYouControl = new PermanentCount(
                new PermanentNamedPredicate("Food Fight"), CountScope.CONTROLLER);

        addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                new ActivatedAbility(
                        false,
                        "{2}",
                        List.of(
                                new SacrificeSelfCost(),
                                new DealDamageToAnyTargetEffect(new Sum(new Fixed(1), foodFightsYouControl))
                        ),
                        "{2}, Sacrifice this artifact: It deals damage to any target equal to 1 plus the number "
                                + "of permanents named Food Fight you control."
                ),
                GrantScope.OWN_PERMANENTS,
                new PermanentIsArtifactPredicate()
        ));
    }
}
