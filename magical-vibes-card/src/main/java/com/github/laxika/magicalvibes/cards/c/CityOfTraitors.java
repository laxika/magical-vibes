package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;

import java.util.List;

@CardRegistration(set = "EXO", collectorNumber = "143")
@CardRegistration(set = "TPR", collectorNumber = "237")
public class CityOfTraitors extends Card {

    public CityOfTraitors() {
        // When you play another land, sacrifice this land.
        addEffect(EffectSlot.ON_CONTROLLER_PLAYS_LAND, new SacrificeSelfEffect());

        // {T}: Add {C}{C}.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.COLORLESS, 2)),
                "{T}: Add {C}{C}."
        ));
    }
}
