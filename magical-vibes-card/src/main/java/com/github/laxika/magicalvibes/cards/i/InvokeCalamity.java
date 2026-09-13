package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CastUpToTwoInstantOrSorceriesFromHandOrGraveyardWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;

@CardRegistration(set = "NEO", collectorNumber = "147")
public class InvokeCalamity extends Card {

    public InvokeCalamity() {
        addEffect(EffectSlot.SPELL,
                new CastUpToTwoInstantOrSorceriesFromHandOrGraveyardWithoutPayingManaCostEffect());
        addEffect(EffectSlot.SPELL, new ExileSpellEffect());
    }
}
