package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GoadTriggeringCreatureUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantDuration;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "NCC", collectorNumber = "7")
@CardRegistration(set = "NCC", collectorNumber = "105")
public class KrosDefenseContractor extends Card {

    public KrosDefenseContractor() {
        target(TargetFilters.creatureAnOpponentControls())
                .addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                        new PutCounterOnTargetPermanentEffect(CounterType.SHIELD, 1));

        addTrigger(Triggers.youPutCountersOnCreatureYouDontControl(),
                new TapPermanentsEffect(TapUntapScope.TRIGGERING),
                new GoadTriggeringCreatureUntilNextTurnEffect(),
                new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.TRIGGERING_PERMANENT,
                        GrantDuration.UNTIL_YOUR_NEXT_TURN));
    }
}
