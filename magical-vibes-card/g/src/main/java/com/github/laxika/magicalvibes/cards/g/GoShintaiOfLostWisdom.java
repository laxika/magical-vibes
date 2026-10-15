package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.PayManaCost;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "NEO", collectorNumber = "55")
public class GoShintaiOfLostWisdom extends Card {

    public GoShintaiOfLostWisdom() {
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, new ForcedCostOrElseEffect(
                new PayManaCost("{1}"),
                List.of(),
                true,
                List.of(new QueueReflexiveAbilityEffect(new MillEffect(
                        new PermanentCount(
                                new PermanentHasSubtypePredicate(CardSubtype.SHRINE), CountScope.CONTROLLER),
                        MillRecipient.TARGET_PLAYER)))));
    }
}
