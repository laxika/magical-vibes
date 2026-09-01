package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SPM", collectorNumber = "8")
@CardRegistration(set = "SPM", collectorNumber = "246")
public class FriendlyNeighborhood extends Card {

    public FriendlyNeighborhood() {
        target(TargetFilters.land())
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CreateTokenEffect(
                        3, "Human Citizen", 1, 1, CardColor.GREEN,
                        Set.of(CardColor.GREEN, CardColor.WHITE),
                        List.of(CardSubtype.HUMAN, CardSubtype.CITIZEN)
                ))
                .addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                        new ActivatedAbility(
                                true,
                                "{1}",
                                List.of(
                                        new BoostTargetCreatureEffect(
                                                new PermanentCount(new PermanentIsCreaturePredicate(), CountScope.CONTROLLER),
                                                new PermanentCount(new PermanentIsCreaturePredicate(), CountScope.CONTROLLER)
                                        )
                                ),
                                "{1}, {T}: Target creature gets +1/+1 until end of turn for each creature you control. "
                                        + "Activate only as a sorcery.",
                                TargetFilters.creature(),
                                null,
                                null,
                                ActivationTimingRestriction.SORCERY_SPEED
                        ),
                        GrantScope.ENCHANTED_PERMANENT
                ));
    }
}
