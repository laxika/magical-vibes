package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;

import java.util.List;

@CardRegistration(set = "RNA", collectorNumber = "180")
public class GyreEngineer extends Card {

    public GyreEngineer() {
        addActivatedAbility(new ActivatedAbility(true, null,
                List.of(new AwardManaEffect(ManaColor.GREEN), new AwardManaEffect(ManaColor.BLUE)),
                "{T}: Add {G}{U}."));
    }
}
