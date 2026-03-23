package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCreatureWithSubtypeToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.TapMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MBS", collectorNumber = "117")
public class MyrTurbine extends Card {

    public MyrTurbine() {
        // {T}: Create a 1/1 colorless Myr artifact creature token.
        addActivatedAbility(new ActivatedAbility(
                true, null,
                List.of(new CreateTokenEffect(
                        1, "Myr", 1, 1, null,
                        List.of(CardSubtype.MYR),
                        Set.of(),
                        Set.of(CardType.ARTIFACT))),
                "{T}: Create a 1/1 colorless Myr artifact creature token."));

        // {T}, Tap five untapped Myr you control: Search your library for a Myr creature card,
        // put it onto the battlefield, then shuffle.
        addActivatedAbility(new ActivatedAbility(
                true, null,
                List.of(
                        new TapMultiplePermanentsCost(5, new PermanentHasSubtypePredicate(CardSubtype.MYR)),
                        new SearchLibraryForCreatureWithSubtypeToBattlefieldEffect(CardSubtype.MYR)),
                "{T}, Tap five untapped Myr you control: Search your library for a Myr creature card, put it onto the battlefield, then shuffle."));
    }
}
