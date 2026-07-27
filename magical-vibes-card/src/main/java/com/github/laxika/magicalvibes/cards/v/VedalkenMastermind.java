package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;

import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "10E", collectorNumber = "123")
public class VedalkenMastermind extends Card {

    public VedalkenMastermind() {
        addActivatedAbility(new ActivatedAbility(
            true,
            "{U}",
            List.of(ReturnToHandEffect.target()),
            "{U}, {T}: Return target permanent you control to its owner's hand.",
            TargetFilters.permanentYouControl()
        ));
    }
}
