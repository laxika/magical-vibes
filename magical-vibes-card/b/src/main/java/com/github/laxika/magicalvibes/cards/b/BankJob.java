package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileBottomCardMatchingMayCastThisTurnAndCreateTokenForStillExiledEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YSNC", collectorNumber = "18")
public class BankJob extends Card {

    public BankJob() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                new ExileBottomCardMatchingMayCastThisTurnAndCreateTokenForStillExiledEffect(
                        new CardTypePredicate(CardType.CREATURE),
                        CreateTokenEffect.ofTreasureToken(1)));
    }
}
