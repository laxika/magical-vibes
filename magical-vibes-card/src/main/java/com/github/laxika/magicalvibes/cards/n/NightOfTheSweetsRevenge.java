package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "WOE", collectorNumber = "178")
public class NightOfTheSweetsRevenge extends Card {

    public NightOfTheSweetsRevenge() {
        PermanentCount foodsYouControl = new PermanentCount(
                new PermanentHasSubtypePredicate(CardSubtype.FOOD), CountScope.CONTROLLER);

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, foodToken());
        addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                ManaAbilities.tapFor(ManaColor.GREEN),
                GrantScope.OWN_PERMANENTS,
                new PermanentHasSubtypePredicate(CardSubtype.FOOD)
        ));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{5}{G}{G}",
                List.of(
                        new SacrificeSelfCost(),
                        new BoostAllOwnCreaturesEffect(foodsYouControl, foodsYouControl)
                ),
                "{5}{G}{G}, Sacrifice this enchantment: Creatures you control get +X/+X until end of turn, "
                        + "where X is the number of Foods you control. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }

    private static CreateTokenEffect foodToken() {
        return CreateTokenEffect.ofArtifactToken(1, "Food", List.of(CardSubtype.FOOD), List.of(
                new ActivatedAbility(
                        true,
                        "{2}",
                        List.of(new SacrificeSelfCost(), new GainLifeEffect(3)),
                        "{2}, {T}, Sacrifice this token: You gain 3 life."
                )
        ));
    }
}
