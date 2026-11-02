package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PlayFromOutsideHandTriggerEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "WHO", collectorNumber = "104")
@CardRegistration(set = "WHO", collectorNumber = "396")
@CardRegistration(set = "WHO", collectorNumber = "709")
@CardRegistration(set = "WHO", collectorNumber = "987")
public class GrahamOBrien extends Card {

    public GrahamOBrien() {
        addTrigger(Triggers.youCastSpell(),
                new PlayFromOutsideHandTriggerEffect(List.of(CreateTokenEffect.ofFoodToken(1))));
    }
}
