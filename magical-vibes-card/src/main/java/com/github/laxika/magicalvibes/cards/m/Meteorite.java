package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

import java.util.List;

@CardRegistration(set = "M15", collectorNumber = "221")
@CardRegistration(set = "ORI", collectorNumber = "233")
@CardRegistration(set = "M21", collectorNumber = "233")
public class Meteorite extends Card {

    public Meteorite() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DealDamageToAnyTargetEffect(2));

        addActivatedAbility(new ActivatedAbility(
                true,                                    // requiresTap
                null,                                    // manaCost
                List.of(new AwardAnyColorManaEffect()),  // effects
                "{T}: Add one mana of any color."        // description
        ));
    }
}
