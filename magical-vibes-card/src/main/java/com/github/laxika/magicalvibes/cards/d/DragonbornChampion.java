package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardIfEventValueAtLeastEffect;

@CardRegistration(set = "SLD", collectorNumber = "2498")
public class DragonbornChampion extends Card {

    public DragonbornChampion() {
        // Whenever a source you control deals 5 or more damage to a player, draw a card.
        addEffect(EffectSlot.ON_ALLY_SOURCE_DEALS_DAMAGE_TO_OPPONENT,
                new DrawCardIfEventValueAtLeastEffect(5));
    }
}
