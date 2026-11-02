package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.LifeLostThisTurn;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MID", collectorNumber = "223")
@CardRegistration(set = "DBL", collectorNumber = "223")
@CardRegistration(set = "DSC", collectorNumber = "217")
public class FlorianVoldarenScion extends Card {

    public FlorianVoldarenScion() {
        addTrigger(Triggers.postcombatMainPhase(ActivePlayer.YOU),
                LookAtTopCardsEffect.chooseOneToExilePlayableRestOnBottomRandom(
                        new LifeLostThisTurn(CountScope.OPPONENTS)));
    }
}
