package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.PermanentLeftBattlefieldUnderYourControlThisTurn;
import com.github.laxika.magicalvibes.model.effect.ExileTopUntilNonlandOfEachOpponentWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastCardExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TMT", collectorNumber = "153")
@CardRegistration(set = "TMT", collectorNumber = "201")
@CardRegistration(set = "TMT", collectorNumber = "245")
public class KrangShredder extends Card {

    public KrangShredder() {
        ExileTopUntilNonlandOfEachOpponentWithSourceEffect exileEffect =
                new ExileTopUntilNonlandOfEachOpponentWithSourceEffect();
        addTrigger(Triggers.enters(ObjectRelation.THIS), exileEffect);
        addTrigger(Triggers.attacks(), exileEffect);
        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                TriggerOptions.DEFAULT.withInterveningIf(new PermanentLeftBattlefieldUnderYourControlThisTurn()),
                new MayCastCardExiledWithSourceEffect());
    }
}
