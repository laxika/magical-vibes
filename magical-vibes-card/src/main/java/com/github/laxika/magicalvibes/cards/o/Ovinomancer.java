package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnMultiplePermanentsToHandCost;
import com.github.laxika.magicalvibes.model.effect.ReturnSelfToHandCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "VIS", collectorNumber = "39")
@CardRegistration(set = "MGB", collectorNumber = "2")
@CardRegistration(set = "TSB", collectorNumber = "27")
public class Ovinomancer extends Card {

    private static final PermanentAllOfPredicate BASIC_LAND = new PermanentAllOfPredicate(List.of(
            new PermanentIsLandPredicate(),
            new PermanentHasSupertypePredicate(CardSupertype.BASIC)));

    public Ovinomancer() {
        // When this creature enters, sacrifice it unless you return three basic lands you control
        // to their owner's hand.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ForcedCostOrElseEffect(
                new ReturnMultiplePermanentsToHandCost(3, BASIC_LAND),
                List.of(new SacrificeSelfEffect()),
                true));

        // {T}, Return this creature to its owner's hand: Destroy target creature. It can't be
        // regenerated. That creature's controller creates a 0/1 green Sheep creature token.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new ReturnSelfToHandCost(),
                        new DestroyTargetPermanentEffect(true, new CreateTokenEffect(
                                "Sheep", 0, 1, CardColor.GREEN,
                                List.of(CardSubtype.SHEEP), Set.of(), Set.of()))
                ),
                "{T}, Return this creature to its owner's hand: Destroy target creature. It can't be "
                        + "regenerated. That creature's controller creates a 0/1 green Sheep creature token.",
                TargetFilters.creature()
        ));
    }
}
