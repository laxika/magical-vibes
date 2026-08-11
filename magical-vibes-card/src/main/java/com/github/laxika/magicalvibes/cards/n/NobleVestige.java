package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;

import java.util.List;

@CardRegistration(set = "ZEN", collectorNumber = "29")
public class NobleVestige extends Card {

    public NobleVestige() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(PreventDamageEffect.nextToTargetPlayerOrPlaneswalker(1)),
                "{T}: Prevent the next 1 damage that would be dealt to target player or planeswalker this turn."
        ));
    }
}
