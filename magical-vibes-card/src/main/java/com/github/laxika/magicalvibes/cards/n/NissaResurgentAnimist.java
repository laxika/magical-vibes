package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.condition.NthAbilityResolutionThisTurn;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.RevealUntilCardPredicateRestOnBottomRandomEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "MAT", collectorNumber = "22")
public class NissaResurgentAnimist extends Card {

    public NissaResurgentAnimist() {
        addEffect(EffectSlot.ON_ALLY_LAND_ENTERS_BATTLEFIELD, new AwardAnyColorManaEffect());
        addEffect(EffectSlot.ON_ALLY_LAND_ENTERS_BATTLEFIELD, ConditionalEffect.unless(
                new NthAbilityResolutionThisTurn(2),
                new RevealUntilCardPredicateRestOnBottomRandomEffect(
                        new CardAnyOfPredicate(List.of(
                                new CardSubtypePredicate(CardSubtype.ELF),
                                new CardSubtypePredicate(CardSubtype.ELEMENTAL))),
                        LibrarySearchDestination.HAND)));
    }
}
