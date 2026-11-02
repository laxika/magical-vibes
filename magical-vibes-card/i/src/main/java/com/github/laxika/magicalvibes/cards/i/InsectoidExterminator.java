package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.PermanentLeftBattlefieldUnderYourControlThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TMT", collectorNumber = "64")
public class InsectoidExterminator extends Card {

    public InsectoidExterminator() {
        addTrigger(Triggers.endStep(ActivePlayer.YOU), new ConditionalEffect(
                new PermanentLeftBattlefieldUnderYourControlThisTurn(), new ScryEffect(1)));
    }
}
