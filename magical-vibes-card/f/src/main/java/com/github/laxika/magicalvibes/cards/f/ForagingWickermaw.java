package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;

import java.util.List;

@CardRegistration(set = "ECL", collectorNumber = "256")
public class ForagingWickermaw extends Card {

    public ForagingWickermaw() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new SurveilEffect(1));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(new AwardAnyColorManaEffect(true)),
                "{1}: Add one mana of any color. This creature becomes that color until end of turn.",
                1
        ));
    }
}
