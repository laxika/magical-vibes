package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

import java.util.List;

@CardRegistration(set = "ONS", collectorNumber = "234")
public class SolarBlast extends Card {

    public SolarBlast() {
        addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(3));

        addHandActivatedAbility(new ActivatedAbility(false, "{1}{R}{R}",
                List.of(new DealDamageToAnyTargetEffect(1), new DrawCardEffect(1)),
                "Cycling {1}{R}{R} ({1}{R}{R}, Discard this card: Draw a card.)",
                null, null, null, null, List.of(), 0, 1));
    }
}
