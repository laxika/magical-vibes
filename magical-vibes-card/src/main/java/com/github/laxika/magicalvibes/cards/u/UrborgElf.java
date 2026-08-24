package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfColorsEffect;

import java.util.List;

@CardRegistration(set = "APC", collectorNumber = "90")
public class UrborgElf extends Card {

    public UrborgElf() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaOfColorsEffect(List.of(ManaColor.BLACK, ManaColor.GREEN, ManaColor.BLUE))),
                "{T}: Add {B}, {G}, or {U}."
        ));
    }
}
