package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;

import java.util.List;

@CardRegistration(set = "9ED", collectorNumber = "204")
public class MoggSentry extends Card {

    public MoggSentry() {
        // Whenever an opponent casts a spell, this creature gets +2/+2 until end of turn.
        addEffect(EffectSlot.ON_OPPONENT_CASTS_SPELL,
                new SpellCastTriggerEffect(null, List.of(new BoostSelfEffect(2, 2))));
    }
}
