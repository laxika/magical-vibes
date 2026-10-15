package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.LookDestination;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

@CardRegistration(set = "TLA", collectorNumber = "44")
public class AccumulateWisdom extends Card {

    public AccumulateWisdom() {
        GraveyardCardThreshold lessonThreshold = new GraveyardCardThreshold(
                3, new CardSubtypePredicate(CardSubtype.LESSON));
        addEffect(EffectSlot.SPELL, new ConditionalEffect(lessonThreshold,
                new LookAtTopCardsEffect(new Fixed(3), new Fixed(3), null,
                        LookDestination.BOTTOM_OF_LIBRARY, false)));
        addEffect(EffectSlot.SPELL, new ConditionalEffect(new NotCondition(lessonThreshold),
                LookAtTopCardsEffect.chooseOneToHandRestOnBottom(new Fixed(3))));
    }
}
