package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerSacrificedPermanentSubtypeAtLeastThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "SOI", collectorNumber = "195")
public class BriarbridgePatrol extends Card {

    public BriarbridgePatrol() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_DEALS_DAMAGE_TO_CREATURE,
                CreateTokenEffect.ofClueToken(1));
        addEffect(EffectSlot.END_STEP_TRIGGERED,
                new ConditionalEffect(
                        new ControllerSacrificedPermanentSubtypeAtLeastThisTurn(3, CardSubtype.CLUE),
                        new PutCardToBattlefieldEffect(new CardTypePredicate(CardType.CREATURE), "creature")));
    }
}
