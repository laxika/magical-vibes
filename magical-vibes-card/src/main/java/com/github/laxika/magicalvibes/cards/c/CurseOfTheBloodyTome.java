package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MillTargetPlayerEffect;

@CardRegistration(set = "ISD", collectorNumber = "50")
public class CurseOfTheBloodyTome extends Card {

    public CurseOfTheBloodyTome() {
        // At the beginning of enchanted player's upkeep, that player mills two cards.
        addEffect(EffectSlot.ENCHANTED_PLAYER_UPKEEP_TRIGGERED, new MillTargetPlayerEffect(2));
    }
}
