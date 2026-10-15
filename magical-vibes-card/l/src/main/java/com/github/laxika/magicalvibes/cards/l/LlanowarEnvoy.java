package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;

import java.util.List;

@CardRegistration(set = "DOM", collectorNumber = "169")
public class LlanowarEnvoy extends Card {

    public LlanowarEnvoy() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{G}",
                List.of(new AwardAnyColorManaEffect()),
                "{1}{G}: Add one mana of any color."
        ));
    }
}
