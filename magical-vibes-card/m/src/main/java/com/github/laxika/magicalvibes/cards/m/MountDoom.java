package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfColorsEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseUpToTwoCreaturesDestroyRestEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

import java.util.List;

@CardRegistration(set = "HOC", collectorNumber = "50")
@CardRegistration(set = "HOC", collectorNumber = "90")
public class MountDoom extends Card {

    public MountDoom() {
        // {T}, Pay 1 life: Add {B} or {R}.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new PayLifeCost(1),
                        new AwardManaOfColorsEffect(List.of(ManaColor.BLACK, ManaColor.RED))
                ),
                "{T}, Pay 1 life: Add {B} or {R}."
        ));

        // {1}{B}{R}, {T}: Mount Doom deals 1 damage to each opponent.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{B}{R}",
                List.of(new DealDamageToPlayersEffect(1, DamageRecipient.EACH_OPPONENT)),
                "{1}{B}{R}, {T}: Mount Doom deals 1 damage to each opponent."
        ));

        // {5}{B}{R}, {T}, Sacrifice Mount Doom and a legendary artifact: Choose up to two creatures, then destroy the rest.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{5}{B}{R}",
                List.of(
                        new SacrificeSelfCost(),
                        new SacrificePermanentCost(
                                new PermanentAllOfPredicate(List.of(
                                        new PermanentIsArtifactPredicate(),
                                        new PermanentHasSupertypePredicate(CardSupertype.LEGENDARY)
                                )),
                                "a legendary artifact",
                                false
                        ),
                        new ChooseUpToTwoCreaturesDestroyRestEffect()
                ),
                "{5}{B}{R}, {T}, Sacrifice Mount Doom and a legendary artifact: Choose up to two creatures, then destroy the rest. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
