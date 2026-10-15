package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfColorsEffect;

import java.util.List;

@CardRegistration(set = "EVE", collectorNumber = "177")
@CardRegistration(set = "EXP", collectorNumber = "35")
@CardRegistration(set = "A25", collectorNumber = "239")
@CardRegistration(set = "2XM", collectorNumber = "318")
@CardRegistration(set = "LTC", collectorNumber = "309")
@CardRegistration(set = "SOC", collectorNumber = "373")
@CardRegistration(set = "DSC", collectorNumber = "276")
@CardRegistration(set = "TDC", collectorNumber = "364")
@CardRegistration(set = "M3C", collectorNumber = "342")
@CardRegistration(set = "OTC", collectorNumber = "297")
@CardRegistration(set = "BLC", collectorNumber = "304")
@CardRegistration(set = "FIC", collectorNumber = "393")
public class FloodedGrove extends Card {

    public FloodedGrove() {
        // {T}: Add {C}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));
        // {G/U}, {T}: Add {G}{G}, {G}{U}, or {U}{U}.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{G/U}",
                List.of(new AwardManaOfColorsEffect(List.of(ManaColor.GREEN, ManaColor.BLUE), 2)),
                "{G/U}, {T}: Add {G}{G}, {G}{U}, or {U}{U}."
        ));
    }
}
