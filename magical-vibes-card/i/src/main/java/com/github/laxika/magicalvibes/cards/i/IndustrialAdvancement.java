package com.github.laxika.magicalvibes.cards.i;

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
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.LookDestination;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "NCC", collectorNumber = "47")
@CardRegistration(set = "NCC", collectorNumber = "147")
public class IndustrialAdvancement extends Card {

    public IndustrialAdvancement() {
        addTrigger(Triggers.endStep(ActivePlayer.YOU), new MayEffect(
                SequenceEffect.of(
                        new SacrificePermanentsEffect(1, new PermanentIsCreaturePredicate(), Subjects.you()),
                        new ConditionalEffect(HappenedThisWay.sacrificed(), new LookAtTopCardsEffect(
                                AmountThisWay.sacrificedManaValue(),
                                new Fixed(1),
                                new CardTypePredicate(CardType.CREATURE),
                                LookDestination.BOTTOM_OF_LIBRARY_RANDOM,
                                false,
                                LibrarySearchDestination.BATTLEFIELD,
                                true))),
                "Sacrifice a creature?"));
    }
}
