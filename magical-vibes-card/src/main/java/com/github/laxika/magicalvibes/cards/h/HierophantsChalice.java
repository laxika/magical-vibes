package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerLosesLifeAndControllerGainsLifeEffect;

import java.util.List;

@CardRegistration(set = "XLN", collectorNumber = "240")
public class HierophantsChalice extends Card {

    public HierophantsChalice() {
        // When Hierophant's Chalice enters the battlefield, target opponent loses 1 life and you gain 1 life.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new TargetPlayerLosesLifeAndControllerGainsLifeEffect(1, 1));

        // {T}: Add {C}.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.COLORLESS)),
                "{T}: Add {C}."
        ));
    }
}
