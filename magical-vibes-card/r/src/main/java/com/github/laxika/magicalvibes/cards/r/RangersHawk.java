package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.TapMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.effect.VentureIntoDungeonEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "AFR", collectorNumber = "37")
public class RangersHawk extends Card {

    public RangersHawk() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}",
                List.of(
                        new TapMultiplePermanentsCost(1, new PermanentIsCreaturePredicate(), true),
                        new VentureIntoDungeonEffect()
                ),
                "{3}, {T}, Tap another untapped creature you control: Venture into the dungeon. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
