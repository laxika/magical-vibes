package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCreatureCardFromGraveyardGainLifeEqualToToughnessEffect;

@CardRegistration(set = "M15", collectorNumber = "112")
public class RotfeasterMaggot extends Card {

    public RotfeasterMaggot() {
        // When this creature enters, exile target creature card from a graveyard. You gain life
        // equal to that card's toughness.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ExileTargetCreatureCardFromGraveyardGainLifeEqualToToughnessEffect());
    }
}
