package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryCardTypeInPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryNotPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "IKO", collectorNumber = "70")
public class VoraciousGreatshark extends Card {

    public VoraciousGreatshark() {
        target(new StackEntryPredicateTargetFilter(
                new StackEntryAllOfPredicate(List.of(
                        new StackEntryCardTypeInPredicate(Set.of(CardType.ARTIFACT, CardType.CREATURE)),
                        new StackEntryNotPredicate(new StackEntryTypeInPredicate(
                                Set.of(StackEntryType.TRIGGERED_ABILITY, StackEntryType.ACTIVATED_ABILITY)))
                )),
                "Target must be an artifact or creature spell."
        )).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CounterSpellEffect());
    }
}
