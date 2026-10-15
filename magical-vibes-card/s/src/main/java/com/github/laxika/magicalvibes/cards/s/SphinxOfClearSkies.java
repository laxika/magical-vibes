package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.BasicLandTypesAmongControlledLands;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardsAndSeparateEffect;

@CardRegistration(set = "DMU", collectorNumber = "67")
public class SphinxOfClearSkies extends Card {

    public SphinxOfClearSkies() {
        addEffect(EffectSlot.ON_BECOMES_TARGET_OF_OPPONENT_SPELL, new CounterUnlessPaysEffect(2));
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new RevealTopCardsAndSeparateEffect(new BasicLandTypesAmongControlledLands()));
    }
}
