package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.ForageEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "BLB", collectorNumber = "197")
public class ThornvaultForager extends Card {

    public ThornvaultForager() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.GREEN));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new ForageEffect(new AwardAnyColorManaEffect(2, true))),
                "{T}, Forage: Add two mana in any combination of colors."
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}{G}",
                List.of(new SearchLibraryEffect(
                        new CardSubtypePredicate(CardSubtype.SQUIRREL),
                        LibrarySearchDestination.HAND)),
                "{3}{G}, {T}: Search your library for a Squirrel card, reveal it, put it into your hand, then shuffle."
        ));
    }
}
