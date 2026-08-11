package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.RevealUntilCardPredicateRestOnBottomRandomEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "INV", collectorNumber = "214")
public class ThicketElemental extends Card {

    public ThicketElemental() {
        addEffect(EffectSlot.STATIC, new KickerEffect("{1}{G}"));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalEffect(new Kicked(),
                new MayEffect(
                        SequenceEffect.of(
                                new RevealUntilCardPredicateRestOnBottomRandomEffect(
                                        new CardTypePredicate(CardType.CREATURE),
                                        LibrarySearchDestination.BATTLEFIELD),
                                new ShuffleLibraryEffect(false)),
                        "Reveal cards until you reveal a creature card?")));
    }
}
