package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;

import java.util.List;

@CardRegistration(set = "ICE", collectorNumber = "126")
public class FoulFamiliar extends Card {

    public FoulFamiliar() {
        // This creature can't block.
        addEffect(EffectSlot.STATIC, new CantBlockEffect());

        // {B}, Pay 1 life: Return this creature to its owner's hand.
        addActivatedAbility(new ActivatedAbility(false, "{B}",
                List.of(new PayLifeCost(1), ReturnToHandEffect.self()),
                "{B}, Pay 1 life: Return this creature to its owner's hand."));
    }
}
