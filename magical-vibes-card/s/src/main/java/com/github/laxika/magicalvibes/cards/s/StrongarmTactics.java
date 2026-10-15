package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachPlayerDiscardsCreatureOrLosesLifeEffect;

@CardRegistration(set = "ONS", collectorNumber = "173")
public class StrongarmTactics extends Card {

    public StrongarmTactics() {
        addEffect(EffectSlot.SPELL, new EachPlayerDiscardsCreatureOrLosesLifeEffect(4));
    }
}
