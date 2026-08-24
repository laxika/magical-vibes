package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.DamageDealtToControllerByArtifactsThisTurn;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "SUM", collectorNumber = "36")
public class ReversePolarity extends Card {

    public ReversePolarity() {
        addEffect(EffectSlot.SPELL, new GainLifeEffect(
                new Scaled(new DamageDealtToControllerByArtifactsThisTurn(), 2)));
    }
}
