package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "TDM", collectorNumber = "248")
public class TemurMonument extends Card {

    public TemurMonument() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new SearchLibraryEffect(
                new CardAllOfPredicate(List.of(
                        new CardSupertypePredicate(CardSupertype.BASIC),
                        new CardTypePredicate(CardType.LAND),
                        new CardAnyOfPredicate(List.of(
                                new CardSubtypePredicate(CardSubtype.FOREST),
                                new CardSubtypePredicate(CardSubtype.ISLAND),
                                new CardSubtypePredicate(CardSubtype.MOUNTAIN))))),
                LibrarySearchDestination.HAND));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}{G}{U}{R}",
                List.of(
                        new SacrificeSelfCost(),
                        new CreateTokenEffect("Elephant", 5, 5, CardColor.GREEN,
                                List.of(CardSubtype.ELEPHANT), Set.of(), Set.of())),
                "{3}{G}{U}{R}, {T}, Sacrifice this artifact: Create a 5/5 green Elephant creature token. "
                        + "Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
