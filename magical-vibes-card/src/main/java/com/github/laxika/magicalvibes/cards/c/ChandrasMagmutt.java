package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect;

import java.util.List;

@CardRegistration(set = "M21", collectorNumber = "137")
public class ChandrasMagmutt extends Card {

    public ChandrasMagmutt() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new DealDamageToTargetPlayerOrPlaneswalkerEffect(1)),
                "{T}: Chandra's Magmutt deals 1 damage to target player or planeswalker."
        ));
    }
}
