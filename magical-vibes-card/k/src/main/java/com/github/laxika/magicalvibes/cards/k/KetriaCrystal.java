package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfColorsEffect;

import java.util.List;

import static com.github.laxika.magicalvibes.model.ManaColor.BLUE;
import static com.github.laxika.magicalvibes.model.ManaColor.GREEN;
import static com.github.laxika.magicalvibes.model.ManaColor.RED;

@CardRegistration(set = "IKO", collectorNumber = "236")
public class KetriaCrystal extends Card {

    public KetriaCrystal() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaOfColorsEffect(List.of(GREEN, BLUE, RED))),
                "{T}: Add {G}, {U}, or {R}."
        ));
        addCycling("{2}");
    }
}
