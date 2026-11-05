package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.condition.NthAbilityResolutionThisTurn;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.RevealUntilCardPredicateRestOnBottomRandomEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MAT", collectorNumber = "22")
@CardRegistration(set = "MAT", collectorNumber = "72")
@CardRegistration(set = "MAT", collectorNumber = "122")
@CardRegistration(set = "MAT", collectorNumber = "162")
public class NissaResurgentAnimist extends Card {

    public NissaResurgentAnimist() {
        addTrigger(Triggers.enters(Triggers.LAND, ObjectRelation.YOU_CONTROL), new AwardAnyColorManaEffect(),
                new ConditionalEffect(
                new NthAbilityResolutionThisTurn(2),
                new RevealUntilCardPredicateRestOnBottomRandomEffect(
                        new CardAnyOfPredicate(List.of(
                                new CardSubtypePredicate(CardSubtype.ELF),
                                new CardSubtypePredicate(CardSubtype.ELEMENTAL))),
                        LibrarySearchDestination.HAND)));
    }
}
