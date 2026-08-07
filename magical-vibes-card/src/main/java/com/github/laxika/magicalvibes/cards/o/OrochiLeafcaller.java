package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;

import java.util.List;

@CardRegistration(set = "CHK", collectorNumber = "234")
public class OrochiLeafcaller extends Card {

    public OrochiLeafcaller() {
        addActivatedAbility(new ActivatedAbility(false, "{G}",
                List.of(new AwardAnyColorManaEffect()),
                "{G}: Add one mana of any color."));
    }
}
