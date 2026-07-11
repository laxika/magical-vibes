package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SetEachPlayerLifeToCreatureCountEffect;

@CardRegistration(set = "9ED", collectorNumber = "231")
public class Biorhythm extends Card {

    public Biorhythm() {
        // Each player's life total becomes the number of creatures they control.
        addEffect(EffectSlot.SPELL, new SetEachPlayerLifeToCreatureCountEffect());
    }
}
