package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.FlipCoinWinEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "EXO", collectorNumber = "88")
public class MoggAssassin extends Card {

    public MoggAssassin() {
        ActivatedAbility ability = new ActivatedAbility(
                true,
                null,
                List.of(new FlipCoinWinEffect(
                        DestroyTargetPermanentEffect.forTargetGroup(0),
                        DestroyTargetPermanentEffect.forTargetGroup(1))),
                "{T}: You choose target creature an opponent controls, and that opponent chooses target creature. "
                        + "Flip a coin. If you win the flip, destroy the creature you chose. If you lose the flip, "
                        + "destroy the creature your opponent chose.",
                List.of(TargetFilters.creatureAnOpponentControls(), TargetFilters.creature()),
                2,
                2
        );
        ability.withOpponentChosenTarget(1, TargetFilters.creature())
                .withAllowSharedTargets();
        addActivatedAbility(ability);
    }
}
