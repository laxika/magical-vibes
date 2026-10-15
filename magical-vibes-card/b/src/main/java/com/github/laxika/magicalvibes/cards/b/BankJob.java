package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileBottomCardMatchingMayCastThisTurnAndCreateTokenForStillExiledEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "YSNC", collectorNumber = "18")
public class BankJob extends Card {

    public BankJob() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new ExileBottomCardMatchingMayCastThisTurnAndCreateTokenForStillExiledEffect(
                        new CardTypePredicate(CardType.CREATURE),
                        CreateTokenEffect.ofTreasureToken(1)));
    }
}
