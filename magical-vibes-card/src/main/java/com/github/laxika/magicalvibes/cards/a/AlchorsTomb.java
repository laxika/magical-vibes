package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SetTargetColorEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "LEG", collectorNumber = "272")
public class AlchorsTomb extends Card {

    public AlchorsTomb() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(new SetTargetColorEffect(null, false, true)),
                "{2}, {T}: Target permanent you control becomes the color of your choice. (This effect lasts indefinitely.)",
                TargetFilters.permanentYouControl()
        ));
    }
}
