package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ControllerCastSpellThisTurn;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "OTJ", collectorNumber = "209")
public class JemLightfooteSkyExplorer extends Card {

    public JemLightfooteSkyExplorer() {
        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                TriggerOptions.DEFAULT.withInterveningIf(
                        new NotCondition(new ControllerCastSpellThisTurn(new CardTruePredicate(), true))),
                new DrawCardEffect());
    }
}
