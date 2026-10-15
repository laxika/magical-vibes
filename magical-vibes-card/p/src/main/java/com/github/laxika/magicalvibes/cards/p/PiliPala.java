package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;

import java.util.List;

@CardRegistration(set = "SHM", collectorNumber = "258")
public class PiliPala extends Card {

    public PiliPala() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(new AwardAnyColorManaEffect()),
                "{2}, {Q}: Add one mana of any color."
        ).withRequiresUntap());
    }
}
