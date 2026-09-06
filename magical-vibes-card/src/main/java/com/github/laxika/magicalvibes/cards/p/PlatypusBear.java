package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.CanAttackAsThoughNoDefenderEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

@CardRegistration(set = "TLA", collectorNumber = "236")
public class PlatypusBear extends Card {

    public PlatypusBear() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MillEffect(2, MillRecipient.CONTROLLER));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new GraveyardCardThreshold(1, new CardSubtypePredicate(CardSubtype.LESSON)),
                new CanAttackAsThoughNoDefenderEffect()));
    }
}
