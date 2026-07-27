package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfColorsEffect;

import java.util.List;

@CardRegistration(set = "SHM", collectorNumber = "280")
public class SunkenRuins extends Card {

    public SunkenRuins() {
        // {T}: Add {C}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));
        // {U/B}, {T}: Add {U}{U}, {U}{B}, or {B}{B}.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{U/B}",
                List.of(new AwardManaOfColorsEffect(List.of(ManaColor.BLUE, ManaColor.BLACK), 2)),
                "{U/B}, {T}: Add {U}{U}, {U}{B}, or {B}{B}."
        ));
    }
}
