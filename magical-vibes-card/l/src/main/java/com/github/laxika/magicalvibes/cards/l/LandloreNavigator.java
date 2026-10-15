package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.PermanentEnteredThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConjureCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "YLCI", collectorNumber = "7")
public class LandloreNavigator extends Card {

    public LandloreNavigator() {
        addEffect(EffectSlot.ON_ATTACK, CreateTokenEffect.ofMapToken(1));
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, new ConditionalEffect(
                new PermanentEnteredThisTurn(new CardTypePredicate(CardType.ARTIFACT), 2),
                new ConjureCardToBattlefieldEffect("Thieving Magpie")));
    }
}
