package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "SOS", collectorNumber = "194")
public class HardenedAcademic extends Card {

    public HardenedAcademic() {
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new DiscardCardTypeCost(null, null),
                        new GrantKeywordEffect(Keyword.LIFELINK, GrantScope.SELF)
                ),
                "Discard a card: This creature gains lifelink until end of turn."
        ));

        target(TargetFilters.creatureYouControl())
                .addTrigger(new TriggerCondition(Triggers.leavesGraveyard(null, ObjectRelation.YOU_OWN)),
                        TriggerOptions.ONE_OR_MORE,
                        new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1));
    }
}
