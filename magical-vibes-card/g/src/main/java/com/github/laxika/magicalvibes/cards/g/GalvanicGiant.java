package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.s.StormReading;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.CardMinManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WOE", collectorNumber = "52")
public class GalvanicGiant extends Card {

    public GalvanicGiant() {
        setBackFaceCard(new StormReading());
        addCastingOption(new AdventureCast("{5}{U}{U}"));

        target(TargetFilters.creatureAnOpponentControls()).addTrigger(Triggers.youCastSpell()
                        .withFilter(new CardMinManaValuePredicate(5)),
                new TapPermanentsEffect(TapUntapScope.TARGET),
                new PutCounterOnTargetPermanentEffect(CounterType.STUN));
    }

    @Override
    public String getBackFaceClassName() {
        return "StormReading";
    }
}
