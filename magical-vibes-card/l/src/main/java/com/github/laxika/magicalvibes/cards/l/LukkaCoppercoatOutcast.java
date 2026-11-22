package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.effect.ControlledCreaturesDealPowerDamageToEachOpponentEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsWithLukkaPermissionEffect;
import com.github.laxika.magicalvibes.model.effect.RevealUntilCardPredicateRestOnBottomRandomEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardManaValueAtMostAmountPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "IKO", collectorNumber = "125")
public class LukkaCoppercoatOutcast extends Card {

    public LukkaCoppercoatOutcast() {
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(new ExileTopCardsWithLukkaPermissionEffect(3)),
                "+1: Exile the top three cards of your library. Creature cards exiled this way gain "
                        + "\u201cYou may cast this card from exile as long as you control a Lukka planeswalker.\u201d"
        ));

        addActivatedAbility(new ActivatedAbility(
                -2,
                List.of(
                        new ExileTargetPermanentEffect(new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentControlledBySourceControllerPredicate()))),
                        new RevealUntilCardPredicateRestOnBottomRandomEffect(
                                new CardAllOfPredicate(List.of(
                                        new CardTypePredicate(CardType.CREATURE),
                                        new CardNotPredicate(new CardManaValueAtMostAmountPredicate(
                                                AmountThisWay.exiledManaValue())))),
                                LibrarySearchDestination.BATTLEFIELD)),
                "\u22122: Exile target creature you control, then reveal cards from the top of your library "
                        + "until you reveal a creature card with greater mana value. Put that card onto the "
                        + "battlefield and the rest on the bottom of your library in a random order.",
                TargetFilters.creatureYouControl()
        ));

        addActivatedAbility(new ActivatedAbility(
                -7,
                List.of(new ControlledCreaturesDealPowerDamageToEachOpponentEffect(
                        new PermanentIsCreaturePredicate())),
                "\u22127: Each creature you control deals damage equal to its power to each opponent."
        ));
    }
}
