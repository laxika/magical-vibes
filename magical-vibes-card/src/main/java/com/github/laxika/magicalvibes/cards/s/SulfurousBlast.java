package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.CastDuringMainPhase;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;

@CardRegistration(set = "TSP", collectorNumber = "180")
public class SulfurousBlast extends Card {

    public SulfurousBlast() {
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new CastDuringMainPhase(), new MassDamageEffect(3, true)));
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new NotCondition(new CastDuringMainPhase()), new MassDamageEffect(2, true)));
    }
}
