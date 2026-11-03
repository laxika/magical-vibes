package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.condition.PermanentEnteredThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConjureCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YLCI", collectorNumber = "7")
public class LandloreNavigator extends Card {

    public LandloreNavigator() {
        addTrigger(Triggers.attacks(), CreateTokenEffect.ofMapToken(1));
        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                TriggerOptions.DEFAULT.withInterveningIf(
                        new PermanentEnteredThisTurn(new CardTypePredicate(CardType.ARTIFACT), 2)),
                new ConjureCardToBattlefieldEffect("UDS", "49"));
    }
}
