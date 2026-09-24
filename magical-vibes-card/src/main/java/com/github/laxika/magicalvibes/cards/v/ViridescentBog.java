package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;

import java.util.List;

@CardRegistration(set = "SOC", collectorNumber = "421")
public class ViridescentBog extends Card {

    public ViridescentBog() {
        // {1}, {T}: Add {B}{G}.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new AwardManaEffect(ManaColor.BLACK), new AwardManaEffect(ManaColor.GREEN)),
                "{1}, {T}: Add {B}{G}."
        ));
    }
}
