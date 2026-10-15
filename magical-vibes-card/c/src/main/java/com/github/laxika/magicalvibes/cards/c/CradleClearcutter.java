package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;

import java.util.List;

@CardRegistration(set = "BRO", collectorNumber = "198")
public class CradleClearcutter extends Card {

    public CradleClearcutter() {
        addPrototype("{2}{G}", CardColor.GREEN, 1, 3);
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.GREEN, new SourcePower())),
                "{T}: Add an amount of {G} equal to Cradle Clearcutter's power."
        ));
    }
}
