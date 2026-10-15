package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfColorsEffect;

import java.util.List;

@CardRegistration(set = "ALA", collectorNumber = "216")
public class ObeliskOfNaya extends Card {

    public ObeliskOfNaya() {
        // {T}: Add {R}, {G}, or {W}.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaOfColorsEffect(List.of(ManaColor.RED, ManaColor.GREEN, ManaColor.WHITE))),
                "{T}: Add {R}, {G}, or {W}."
        ));
    }
}
