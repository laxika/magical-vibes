package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "M19", collectorNumber = "235")
public class FountainOfRenewal extends Card {

    public FountainOfRenewal() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new GainLifeEffect(1));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}",
                List.of(new SacrificeSelfCost(), new DrawCardEffect(1)),
                "{3}, Sacrifice this artifact: Draw a card."
        ));
    }
}
