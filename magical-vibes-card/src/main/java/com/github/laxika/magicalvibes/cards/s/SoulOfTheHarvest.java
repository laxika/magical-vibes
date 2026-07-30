package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "AVR", collectorNumber = "195")
public class SoulOfTheHarvest extends Card {

    public SoulOfTheHarvest() {
        // The slot skips tokens and the entering permanent itself, covering "another nontoken".
        addEffect(EffectSlot.ON_ALLY_NONTOKEN_CREATURE_ENTERS_BATTLEFIELD,
                new MayEffect(new DrawCardEffect(), "Draw a card?"));
    }
}
