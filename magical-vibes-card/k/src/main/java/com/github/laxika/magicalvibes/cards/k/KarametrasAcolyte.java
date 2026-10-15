package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.ColorManaSymbolsAmongControlledPermanents;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;

import java.util.List;

@CardRegistration(set = "THS", collectorNumber = "160")
public class KarametrasAcolyte extends Card {

    public KarametrasAcolyte() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(
                        ManaColor.GREEN,
                        new ColorManaSymbolsAmongControlledPermanents(ManaColor.GREEN)
                )),
                "{T}: Add an amount of {G} equal to your devotion to green."
        ));
    }
}
