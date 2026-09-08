package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;

import java.util.List;

@CardRegistration(set = "APC", collectorNumber = "44")
public class NecraDisciple extends Card {

    public NecraDisciple() {
        addActivatedAbility(new ActivatedAbility(
                true, "{G}",
                List.of(new AwardAnyColorManaEffect()),
                "{G}, {T}: Add one mana of any color."
        ));

        addActivatedAbility(new ActivatedAbility(
                true, "{W}",
                List.of(PreventDamageEffect.nextToTarget(1)),
                "{W}, {T}: Prevent the next 1 damage that would be dealt to any target this turn."
        ));
    }
}
