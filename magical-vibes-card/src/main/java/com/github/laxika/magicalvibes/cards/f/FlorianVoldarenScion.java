package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.LifeLostThisTurn;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;

@CardRegistration(set = "MID", collectorNumber = "223")
public class FlorianVoldarenScion extends Card {

    public FlorianVoldarenScion() {
        addEffect(EffectSlot.POSTCOMBAT_MAIN_TRIGGERED,
                LookAtTopCardsEffect.chooseOneToExilePlayableRestOnBottomRandom(
                        new LifeLostThisTurn(CountScope.OPPONENTS)));
    }
}
