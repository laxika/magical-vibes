package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

import java.util.List;

@CardRegistration(set = "BRO", collectorNumber = "234")
public class EnergyRefractor extends Card {

    public EnergyRefractor() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DrawCardEffect());

        addActivatedAbility(new ActivatedAbility(
                false,                                      // requiresTap
                "{2}",                                      // manaCost
                List.of(new AwardAnyColorManaEffect()),     // effects
                "{2}: Add one mana of any color."            // description
        ));
    }
}
