package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToTargetUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.RevealUntilCardPredicateRestOnBottomRandomEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "WHO", collectorNumber = "59")
@CardRegistration(set = "WHO", collectorNumber = "664")
public class TimeLordRegeneration extends Card {

    private static final CardPredicate TIME_LORD_CREATURE_CARD = new CardAllOfPredicate(List.of(
            new CardTypePredicate(CardType.CREATURE),
            new CardSubtypePredicate(CardSubtype.TIME_LORD)));

    public TimeLordRegeneration() {
        target(new ControlledPermanentPredicateTargetFilter(
                new PermanentHasSubtypePredicate(CardSubtype.TIME_LORD),
                "Target must be a Time Lord you control")).addEffect(EffectSlot.SPELL,
                new GrantEffectToTargetUntilEndOfTurnEffect(
                        EffectSlot.ON_DEATH,
                        new RevealUntilCardPredicateRestOnBottomRandomEffect(
                                TIME_LORD_CREATURE_CARD, LibrarySearchDestination.BATTLEFIELD)));
    }
}
