package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;

import java.util.List;

@CardRegistration(set = "MMQ", collectorNumber = "188")
public class FlailingOgre extends Card {

    public FlailingOgre() {
        // {1}: This creature gets +1/+1 until end of turn. Any player may activate this ability.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(BoostEffect.self(1, 1)),
                "{1}: This creature gets +1/+1 until end of turn. Any player may activate this ability."
        ).withActivatableByAnyPlayer());

        // {1}: This creature gets -1/-1 until end of turn. Any player may activate this ability.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(BoostEffect.self(-1, -1)),
                "{1}: This creature gets -1/-1 until end of turn. Any player may activate this ability."
        ).withActivatableByAnyPlayer());
    }
}
