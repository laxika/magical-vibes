package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.SacrificedPermanentManaValue;
import com.github.laxika.magicalvibes.model.effect.ManaValueBound;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentThenEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "THB", collectorNumber = "215")
public class EnigmaticIncarnation extends Card {

    public EnigmaticIncarnation() {
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, new MayEffect(
                new SacrificePermanentThenEffect(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsEnchantmentPredicate(),
                                new PermanentNotPredicate(new PermanentIsSourceCardPredicate())
                        )),
                        new SearchLibraryEffect(
                                new CardTypePredicate(CardType.CREATURE),
                                LibrarySearchDestination.BATTLEFIELD,
                                new ManaValueBound(new SacrificedPermanentManaValue(), true, 1)),
                        "another enchantment",
                        false,
                        false),
                "Sacrifice another enchantment?"));
    }
}
