package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfColorsEffect;

import java.util.List;

@CardRegistration(set = "EVE", collectorNumber = "175")
public class CascadeBluffs extends Card {

    public CascadeBluffs() {
        // {T}: Add {C}.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.COLORLESS)),
                "{T}: Add {C}."
        ));
        // {U/R}, {T}: Add {U}{U}, {U}{R}, or {R}{R}.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{U/R}",
                List.of(new AwardManaOfColorsEffect(List.of(ManaColor.BLUE, ManaColor.RED), 2)),
                "{U/R}, {T}: Add {U}{U}, {U}{R}, or {R}{R}."
        ));
    }
}
