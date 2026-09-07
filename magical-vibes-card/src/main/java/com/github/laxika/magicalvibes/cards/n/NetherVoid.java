package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;

import java.util.List;

@CardRegistration(set = "LEG", collectorNumber = "113")
public class NetherVoid extends Card {

    public NetherVoid() {
        addEffect(EffectSlot.ON_ANY_PLAYER_CASTS_SPELL,
                new SpellCastTriggerEffect(null, List.of(new CounterUnlessPaysEffect(3))));
    }
}
