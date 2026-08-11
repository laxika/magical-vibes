package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;

import java.util.List;

@CardRegistration(set = "ODY", collectorNumber = "326")
public class ShadowbloodRidge extends Card {

    public ShadowbloodRidge() {
        // {1}, {T}: Add {B}{R}.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(
                        new AwardManaEffect(ManaColor.BLACK),
                        new AwardManaEffect(ManaColor.RED)
                ),
                "{1}, {T}: Add {B}{R}."
        ));
    }
}
