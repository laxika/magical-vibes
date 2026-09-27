package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ModifyCastCostForCardsDrawnThisTurnEffect;

@CardRegistration(set = "YMID", collectorNumber = "3")
public class CaptainEberhart extends Card {

    public CaptainEberhart() {
        // Spells cast from cards you drew this turn cost {1} less, and spells cast from cards your
        // opponents drew this turn cost {1} more.
        addEffect(EffectSlot.STATIC, new ModifyCastCostForCardsDrawnThisTurnEffect(1, false));
        addEffect(EffectSlot.STATIC, new ModifyCastCostForCardsDrawnThisTurnEffect(1, true));
    }
}
