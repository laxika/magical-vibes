package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;

import java.util.List;

@CardRegistration(set = "RAV", collectorNumber = "262")
public class GolgariSignet extends Card {

    public GolgariSignet() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new AwardManaEffect(ManaColor.BLACK), new AwardManaEffect(ManaColor.GREEN)),
                "{1}, {T}: Add {B}{G}."
        ));
    }
}
