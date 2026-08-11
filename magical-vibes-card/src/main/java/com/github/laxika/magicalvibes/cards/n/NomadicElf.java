package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;

import java.util.List;

@CardRegistration(set = "INV", collectorNumber = "200")
public class NomadicElf extends Card {

    public NomadicElf() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{G}",
                List.of(new AwardAnyColorManaEffect()),
                "{1}{G}: Add one mana of any color."
        ));
    }
}
