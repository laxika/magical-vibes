package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfColorsEffect;

import java.util.List;

@CardRegistration(set = "SHM", collectorNumber = "272")
public class GravenCairns extends Card {

    public GravenCairns() {
        // {T}: Add {C}.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.COLORLESS)),
                "{T}: Add {C}."
        ));
        // {B/R}, {T}: Add {B}{B}, {B}{R}, or {R}{R}.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{B/R}",
                List.of(new AwardManaOfColorsEffect(List.of(ManaColor.BLACK, ManaColor.RED), 2)),
                "{B/R}, {T}: Add {B}{B}, {B}{R}, or {R}{R}."
        ));
    }
}
