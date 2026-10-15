package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerMayExileGraveyardCardsThenLoseLifeEffect;

@CardRegistration(set = "JUD", collectorNumber = "67")
public class GraveConsequences extends Card {

    public GraveConsequences() {
        addEffect(EffectSlot.SPELL, new EachPlayerMayExileGraveyardCardsThenLoseLifeEffect());
        addEffect(EffectSlot.SPELL, new DrawCardEffect(1));
    }
}
