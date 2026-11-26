package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "USG", collectorNumber = "136")
@CardRegistration(set = "DMR", collectorNumber = "86")
public class FleshReaver extends Card {

    public FleshReaver() {
        DealDamageEffect damageBack =
                DealDamageEffect.to(Subjects.you(), new EventValue());
        addTrigger(List.of(
                        Triggers.dealsDamageToCreature(),
                        Triggers.sourceYouControlDealsDamageToOpponent()
                                .withFilter(new PermanentIsSourcePermanentPredicate())),
                damageBack);
    }
}
