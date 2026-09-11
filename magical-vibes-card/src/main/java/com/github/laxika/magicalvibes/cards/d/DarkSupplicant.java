package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.SacrificeMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.effect.SearchZonesForCardNamedToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "LGN", collectorNumber = "64")
public class DarkSupplicant extends Card {

    public DarkSupplicant() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new SacrificeMultiplePermanentsCost(3,
                                new PermanentHasSubtypePredicate(CardSubtype.CLERIC)),
                        new SearchZonesForCardNamedToBattlefieldEffect("Scion of Darkness")
                ),
                "{T}, Sacrifice three Clerics: Search your graveyard, hand, and/or library for a card "
                        + "named Scion of Darkness and put it onto the battlefield. If you search your "
                        + "library this way, shuffle."
        ));
    }
}
