package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PlayFromOutsideHandTriggerEffect;

import java.util.List;

@CardRegistration(set = "WHO", collectorNumber = "104")
@CardRegistration(set = "WHO", collectorNumber = "396")
@CardRegistration(set = "WHO", collectorNumber = "709")
@CardRegistration(set = "WHO", collectorNumber = "987")
public class GrahamOBrien extends Card {

    public GrahamOBrien() {
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new PlayFromOutsideHandTriggerEffect(List.of(CreateTokenEffect.ofFoodToken(1))));
    }
}
