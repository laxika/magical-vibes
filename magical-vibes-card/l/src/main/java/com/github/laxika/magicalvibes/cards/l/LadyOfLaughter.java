package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.condition.PermanentEnteredThisTurn;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WOE", collectorNumber = "309")
@CardRegistration(set = "WOE", collectorNumber = "370")
public class LadyOfLaughter extends Card {

    public LadyOfLaughter() {
        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                TriggerOptions.DEFAULT.withInterveningIf(
                        new PermanentEnteredThisTurn(new CardNotPredicate(new CardTypePredicate(CardType.LAND)), 2)),
                new DrawCardEffect());
    }
}
