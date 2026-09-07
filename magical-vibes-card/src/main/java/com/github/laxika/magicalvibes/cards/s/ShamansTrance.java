package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AllowPlayFromOtherGraveyardsThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.OtherPlayersCantPlayFromGraveyardsThisTurnEffect;

@CardRegistration(set = "JUD", collectorNumber = "98")
public class ShamansTrance extends Card {

    public ShamansTrance() {
        addEffect(EffectSlot.SPELL, new OtherPlayersCantPlayFromGraveyardsThisTurnEffect());
        addEffect(EffectSlot.SPELL, new AllowPlayFromOtherGraveyardsThisTurnEffect());
    }
}
