package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.MillTargetPlayerEffect;

import java.util.List;

@CardRegistration(set = "ISD", collectorNumber = "245")
public class NephaliaDrownyard extends Card {

    public NephaliaDrownyard() {
        // {T}: Add {C}.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.COLORLESS)),
                "{T}: Add {C}."
        ));

        // {1}{U}{B}, {T}: Target player mills three cards.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{U}{B}",
                List.of(new MillTargetPlayerEffect(3)),
                "{1}{U}{B}, {T}: Target player mills three cards."
        ));
    }
}
