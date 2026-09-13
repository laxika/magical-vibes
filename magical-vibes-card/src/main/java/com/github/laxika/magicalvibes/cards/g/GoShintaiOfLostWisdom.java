package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "NEO", collectorNumber = "55")
public class GoShintaiOfLostWisdom extends Card {

    public GoShintaiOfLostWisdom() {
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                MayPayManaEffect.reflexiveTarget("{1}", new MillEffect(
                        new PermanentCount(
                                new PermanentHasSubtypePredicate(CardSubtype.SHRINE), CountScope.CONTROLLER),
                        MillRecipient.TARGET_PLAYER), "Pay {1} to have target player mill?"));
    }
}
