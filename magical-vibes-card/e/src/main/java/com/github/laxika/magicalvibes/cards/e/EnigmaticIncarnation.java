package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.ManaValueBound;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "THB", collectorNumber = "215")
@CardRegistration(set = "THB", collectorNumber = "337")
public class EnigmaticIncarnation extends Card {

    public EnigmaticIncarnation() {
        addTrigger(Triggers.endStep(ActivePlayer.YOU), new MayEffect(
                SequenceEffect.of(new SacrificePermanentsEffect(1, new PermanentAllOfPredicate(List.of(
                                new PermanentIsEnchantmentPredicate(),
                                new PermanentNotPredicate(new PermanentIsSourceCardPredicate())
                        )), Subjects.you()),
                        new ConditionalEffect(HappenedThisWay.sacrificed(), new SearchLibraryEffect(
                                new CardTypePredicate(CardType.CREATURE),
                                LibrarySearchDestination.BATTLEFIELD,
                                new ManaValueBound(AmountThisWay.sacrificedManaValue(), true, 1)))),
                "Sacrifice another enchantment?"));
    }
}
