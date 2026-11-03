package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.condition.ControllerCastSpellThisTurn;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardsForCardTypesCastThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BRO", collectorNumber = "51")
public class HurkylMasterWizard extends Card {

    public HurkylMasterWizard() {
        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                TriggerOptions.DEFAULT.withInterveningIf(
                        new ControllerCastSpellThisTurn(new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)))),
                new RevealTopCardsForCardTypesCastThisTurnEffect(5));
    }
}
