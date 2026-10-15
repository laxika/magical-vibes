package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.PermanentEnteredThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "SNC", collectorNumber = "211")
public class ParkHeightsPegasus extends Card {

    public ParkHeightsPegasus() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, ConditionalEffect.unless(
                new PermanentEnteredThisTurn(new CardTypePredicate(CardType.CREATURE), 2),
                new DrawCardEffect(1)));
    }
}
