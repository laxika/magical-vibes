package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

import java.util.List;

@CardRegistration(set = "MMQ", collectorNumber = "189")
public class FlailingSoldier extends Card {

    public FlailingSoldier() {
        // {1}: This creature gets +1/+1 until end of turn. Any player may activate this ability.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(new BoostSelfEffect(1, 1)),
                "{1}: This creature gets +1/+1 until end of turn. Any player may activate this ability."
        ).withActivatableByAnyPlayer());

        // {1}: This creature gets -1/-1 until end of turn. Any player may activate this ability.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(new BoostSelfEffect(-1, -1)),
                "{1}: This creature gets -1/-1 until end of turn. Any player may activate this ability."
        ).withActivatableByAnyPlayer());
    }
}
