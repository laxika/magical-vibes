package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.PermanentLeftBattlefieldUnderYourControlThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardCreatureToBattlefieldOrMayBottomEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "AER", collectorNumber = "105")
@CardRegistration(set = "MOC", collectorNumber = "290")
public class AidFromTheCowl extends Card {

    public AidFromTheCowl() {
        addTrigger(Triggers.endStep(ActivePlayer.YOU), new ConditionalEffect(
                new PermanentLeftBattlefieldUnderYourControlThisTurn(),
                new RevealTopCardCreatureToBattlefieldOrMayBottomEffect(new CardIsPermanentPredicate(), true)));
    }
}
