package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "PTK", collectorNumber = "36")
@CardRegistration(set = "ME3", collectorNumber = "32")
@CardRegistration(set = "GS1", collectorNumber = "17")
public class BrilliantPlan extends Card {

    public BrilliantPlan() {
        addEffect(EffectSlot.SPELL, new DrawCardEffect(3));
    }
}
