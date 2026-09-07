package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MillControllerAndPutAnyNumberOfMilledCreaturesIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentThenEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCreatureWithOneMoreManaValueThanSacrificedPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SNC", collectorNumber = "162")
public class VivienOnTheHunt extends Card {

    public VivienOnTheHunt() {
        addActivatedAbility(new ActivatedAbility(
                +2,
                List.of(new MayEffect(
                        new SacrificePermanentThenEffect(
                                new PermanentIsCreaturePredicate(),
                                new SearchLibraryForCreatureWithOneMoreManaValueThanSacrificedPermanentEffect(),
                                "a creature",
                                false,
                                false),
                        "Sacrifice a creature?")),
                "+2: You may sacrifice a creature. If you do, search your library for a creature card "
                        + "with mana value equal to 1 plus the sacrificed creature's mana value, put it "
                        + "onto the battlefield, then shuffle."
        ));

        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(new MillControllerAndPutAnyNumberOfMilledCreaturesIntoHandEffect(5)),
                "+1: Mill five cards, then put any number of creature cards milled this way into your hand."
        ));

        addActivatedAbility(new ActivatedAbility(
                -1,
                List.of(new CreateTokenEffect(
                        "Rhino", 4, 4, CardColor.GREEN,
                        List.of(CardSubtype.RHINO, CardSubtype.WARRIOR), Set.of(), Set.of())),
                "−1: Create a 4/4 green Rhino Warrior creature token."
        ));
    }
}
