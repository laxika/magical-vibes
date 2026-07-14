package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfColorsEffect;

import java.util.List;

@CardRegistration(set = "EVE", collectorNumber = "176")
public class FetidHeath extends Card {

    public FetidHeath() {
        // {T}: Add {C}.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.COLORLESS)),
                "{T}: Add {C}."
        ));
        // {W/B}, {T}: Add {W}{W}, {W}{B}, or {B}{B}.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{W/B}",
                List.of(new AwardManaOfColorsEffect(List.of(ManaColor.WHITE, ManaColor.BLACK), 2)),
                "{W/B}, {T}: Add {W}{W}, {W}{B}, or {B}{B}."
        ));
    }
}
