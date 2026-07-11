package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.IncreaseSpellCostExceptOnControllersTurnEffect;

@CardRegistration(set = "9ED", collectorNumber = "293")
public class DefenseGrid extends Card {

    public DefenseGrid() {
        addEffect(EffectSlot.STATIC, new IncreaseSpellCostExceptOnControllersTurnEffect(3));
    }
}
