package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.OpponentLostLifeThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsChooseOneMayPlayThisTurnEffect;

@CardRegistration(set = "BLB", collectorNumber = "213")
public class FireglassMentor extends Card {

    public FireglassMentor() {
        addEffect(EffectSlot.POSTCOMBAT_MAIN_TRIGGERED, new ConditionalEffect(
                new OpponentLostLifeThisTurn(1),
                new ExileTopCardsChooseOneMayPlayThisTurnEffect(2)));
    }
}
