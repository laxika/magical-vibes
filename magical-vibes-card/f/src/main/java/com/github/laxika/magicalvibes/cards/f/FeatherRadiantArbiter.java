package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CopySpellForEachOtherCreatureWithManaEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsOnlySinglePermanentOrPlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsSourcePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MKC", collectorNumber = "6")
@CardRegistration(set = "MKC", collectorNumber = "313")
public class FeatherRadiantArbiter extends Card {

    public FeatherRadiantArbiter() {
        addTrigger(Triggers.youCastSpell()
                        .withFilter(new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)))
                        .withStackEntryFilter(new StackEntryAllOfPredicate(List.of(
                                new StackEntryTargetsOnlySinglePermanentOrPlayerPredicate(),
                                new StackEntryTargetsSourcePredicate()))),
                new CopySpellForEachOtherCreatureWithManaEffect("{2}"));
    }
}
