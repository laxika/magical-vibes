package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "EOE", collectorNumber = "22")
public class HonoredKnightCaptain extends Card {

    public HonoredKnightCaptain() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CreateTokenEffect(
                1,
                "Human Soldier",
                1,
                1,
                CardColor.WHITE,
                List.of(CardSubtype.HUMAN, CardSubtype.SOLDIER),
                Set.of(),
                Set.of()
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}{W}{W}",
                List.of(
                        new SacrificeSelfCost(),
                        new SearchLibraryEffect(
                                new CardSubtypePredicate(CardSubtype.EQUIPMENT),
                                LibrarySearchDestination.BATTLEFIELD
                        )
                ),
                "{4}{W}{W}, Sacrifice this creature: Search your library for an Equipment card, put it onto the battlefield, then shuffle."
        ));
    }
}
