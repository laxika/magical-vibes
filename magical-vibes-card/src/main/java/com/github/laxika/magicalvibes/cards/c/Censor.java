package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

import java.util.List;

@CardRegistration(set = "AKH", collectorNumber = "46")
public class Censor extends Card {

    public Censor() {
        addEffect(EffectSlot.SPELL, new CounterUnlessPaysEffect(1));

        // Cycling {U} ({U}, Discard this card: Draw a card.) — discard cost is intrinsic.
        addHandActivatedAbility(new ActivatedAbility(false, "{U}",
                List.of(new DrawCardEffect(1)),
                "Cycling {U} ({U}, Discard this card: Draw a card.)"));
    }
}
