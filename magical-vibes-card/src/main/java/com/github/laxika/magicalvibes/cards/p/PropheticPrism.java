package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

import java.util.List;

@CardRegistration(set = "GTC", collectorNumber = "234")
@CardRegistration(set = "KLD", collectorNumber = "229")
@CardRegistration(set = "ROE", collectorNumber = "222")
@CardRegistration(set = "ONE", collectorNumber = "238")
public class PropheticPrism extends Card {

    public PropheticPrism() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DrawCardEffect(1));

        addActivatedAbility(new ActivatedAbility(
                true,                                        // requiresTap
                "{1}",                                       // manaCost
                List.of(new AwardAnyColorManaEffect()),      // effects
                "{1}, {T}: Add one mana of any color."       // description
        ));
    }
}
