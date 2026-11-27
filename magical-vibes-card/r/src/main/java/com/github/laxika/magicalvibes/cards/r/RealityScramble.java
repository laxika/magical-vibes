package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.Retrace;
import com.github.laxika.magicalvibes.model.effect.PutTargetOnBottomOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.RevealUntilCardPredicateRestOnBottomRandomEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardSharesCardTypeWithPermanentsPredicate;
import com.github.laxika.magicalvibes.model.filter.OwnedPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

@CardRegistration(set = "C18", collectorNumber = "25")
public class RealityScramble extends Card {

    public RealityScramble() {
        // "A card that shares a card type with that permanent": the types it had as it last existed on the
        // battlefield (its ruling, CR 608.2h).
        target(new OwnedPermanentPredicateTargetFilter(
                new PermanentTruePredicate(), "Target must be a permanent you own"))
                .addSpellEffect(new PutTargetOnBottomOfLibraryEffect());
        addSpellEffect(new RevealUntilCardPredicateRestOnBottomRandomEffect(
                new CardSharesCardTypeWithPermanentsPredicate(Subjects.targetDeclaredElsewhere()),
                LibrarySearchDestination.BATTLEFIELD));
        addCastingOption(new Retrace());
    }
}
