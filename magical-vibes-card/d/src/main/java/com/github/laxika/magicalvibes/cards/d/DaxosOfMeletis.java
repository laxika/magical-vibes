package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedByCreaturesMatchingPredicateEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "THS", collectorNumber = "191")
@CardRegistration(set = "NCC", collectorNumber = "335")
@CardRegistration(set = "C18", collectorNumber = "173")
public class DaxosOfMeletis extends Card {

    public DaxosOfMeletis() {
        addStaticEffect(new CantBeBlockedByCreaturesMatchingPredicateEffect(
                new PermanentPowerAtLeastPredicate(3)));
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                SequenceEffect.of(
                        new ExileTopCardMayPlayThisTurnEffect(1, false)
                                .fromLibraryOf(Subjects.triggeringPlayer())
                                .mayOnlyCast().withAnyManaType(),
                        new GainLifeEffect(AmountThisWay.exiledManaValue(), Subjects.you())));
    }
}
