package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfColorsEffect;

import java.util.List;

@CardRegistration(set = "SHM", collectorNumber = "277")
public class MysticGate extends Card {

    public MysticGate() {
        // {T}: Add {C}.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.COLORLESS)),
                "{T}: Add {C}."
        ));
        // {W/U}, {T}: Add {W}{W}, {W}{U}, or {U}{U}.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{W/U}",
                List.of(new AwardManaOfColorsEffect(List.of(ManaColor.WHITE, ManaColor.BLUE), 2)),
                "{W/U}, {T}: Add {W}{W}, {W}{U}, or {U}{U}."
        ));
    }
}
