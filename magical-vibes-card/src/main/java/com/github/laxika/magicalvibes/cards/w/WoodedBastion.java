package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfColorsEffect;

import java.util.List;

@CardRegistration(set = "SHM", collectorNumber = "281")
public class WoodedBastion extends Card {

    public WoodedBastion() {
        // {T}: Add {C}.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.COLORLESS)),
                "{T}: Add {C}."
        ));
        // {G/W}, {T}: Add {G}{G}, {G}{W}, or {W}{W}.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{G/W}",
                List.of(new AwardManaOfColorsEffect(List.of(ManaColor.GREEN, ManaColor.WHITE), 2)),
                "{G/W}, {T}: Add {G}{G}, {G}{W}, or {W}{W}."
        ));
    }
}
