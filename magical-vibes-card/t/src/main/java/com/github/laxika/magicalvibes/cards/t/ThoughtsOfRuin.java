package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

@CardRegistration(set = "SOK", collectorNumber = "118")
public class ThoughtsOfRuin extends Card {

    public ThoughtsOfRuin() {
        addEffect(EffectSlot.SPELL, new SacrificePermanentsEffect(
                new CardsInHand(CountScope.CONTROLLER), new PermanentIsLandPredicate(), SacrificeRecipient.EACH_PLAYER));
    }
}
