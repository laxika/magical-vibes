package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfColorsEffect;

import java.util.List;

@CardRegistration(set = "EVE", collectorNumber = "180")
public class TwilightMire extends Card {

    public TwilightMire() {
        // {T}: Add {C}.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.COLORLESS)),
                "{T}: Add {C}."
        ));
        // {B/G}, {T}: Add {B}{B}, {B}{G}, or {G}{G}.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{B/G}",
                List.of(new AwardManaOfColorsEffect(List.of(ManaColor.BLACK, ManaColor.GREEN), 2)),
                "{B/G}, {T}: Add {B}{B}, {B}{G}, or {G}{G}."
        ));
    }
}
