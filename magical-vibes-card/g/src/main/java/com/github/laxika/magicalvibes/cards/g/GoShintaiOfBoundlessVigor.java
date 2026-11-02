package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "NEO", collectorNumber = "187")
public class GoShintaiOfBoundlessVigor extends Card {

    public GoShintaiOfBoundlessVigor() {
        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                new MayPayManaEffect("{1}",
                        new QueueReflexiveAbilityEffect(new PutCounterOnTargetPermanentEffect(
                                CounterType.PLUS_ONE_PLUS_ONE,
                                new PermanentCount(
                                        new PermanentHasSubtypePredicate(CardSubtype.SHRINE),
                                        CountScope.CONTROLLER),
                                null,
                                new PermanentHasSubtypePredicate(CardSubtype.SHRINE),
                                false,
                                null)),
                        "Pay {1} to put a +1/+1 counter on target Shrine for each Shrine you control?"));
    }
}
