package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.OpponentLostLifeThisTurn;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsChooseOneMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BLB", collectorNumber = "213")
public class FireglassMentor extends Card {

    public FireglassMentor() {
        addTrigger(Triggers.secondMainPhase(ActivePlayer.YOU),
                TriggerOptions.DEFAULT.withInterveningIf(new OpponentLostLifeThisTurn(1)),
                new ExileTopCardsChooseOneMayPlayThisTurnEffect(2));
    }
}
