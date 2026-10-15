package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.SacrificedPermanentManaValue;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.LookDestination;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentThenEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "NCC", collectorNumber = "47")
@CardRegistration(set = "NCC", collectorNumber = "147")
public class IndustrialAdvancement extends Card {

    public IndustrialAdvancement() {
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, new MayEffect(
                new SacrificePermanentThenEffect(
                        new PermanentIsCreaturePredicate(),
                        new LookAtTopCardsEffect(
                                new SacrificedPermanentManaValue(),
                                new Fixed(1),
                                new CardTypePredicate(CardType.CREATURE),
                                LookDestination.BOTTOM_OF_LIBRARY_RANDOM,
                                false,
                                LibrarySearchDestination.BATTLEFIELD,
                                true),
                        "a creature",
                        false,
                        false),
                "Sacrifice a creature?"));
    }
}
