package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ISD", collectorNumber = "138")
public class CurseOfThePiercedHeart extends Card {

    public CurseOfThePiercedHeart() {
        addTrigger(Triggers.upkeep(ActivePlayer.ENCHANTED_PLAYER),
                DealDamageEffect.toPlayerOrTheirPlaneswalker(1, Subjects.triggeringPlayer()));
    }
}
