package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.CardsInLibrary;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEachMatchingPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.EachPermanentScope;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "M19", collectorNumber = "296")
public class SarkhanDragonsoul extends Card {

    public SarkhanDragonsoul() {
        addActivatedAbility(new ActivatedAbility(
                +2,
                List.of(
                        new DealDamageToPlayersEffect(1, DamageRecipient.EACH_OPPONENT),
                        new DealDamageToEachMatchingPermanentEffect(
                                1,
                                new PermanentAllOfPredicate(List.of(
                                        new PermanentIsCreaturePredicate(),
                                        new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate()))),
                                EachPermanentScope.ALL_PLAYERS)),
                "+2: Sarkhan, Dragonsoul deals 1 damage to each opponent and each creature your opponents control."
        ));

        addActivatedAbility(new ActivatedAbility(
                -3,
                List.of(new DealDamageToTargetPlayerOrPlaneswalkerEffect(4)),
                "−3: Sarkhan, Dragonsoul deals 4 damage to target player or planeswalker."
        ));

        addActivatedAbility(new ActivatedAbility(
                -9,
                List.of(new SearchLibraryEffect(
                        new CardsInLibrary(CountScope.CONTROLLER),
                        new CardAllOfPredicate(List.of(
                                new CardTypePredicate(CardType.CREATURE),
                                new CardSubtypePredicate(CardSubtype.DRAGON))),
                        LibrarySearchDestination.BATTLEFIELD)),
                "−9: Search your library for any number of Dragon creature cards, put them onto the battlefield, then shuffle."
        ));
    }
}
