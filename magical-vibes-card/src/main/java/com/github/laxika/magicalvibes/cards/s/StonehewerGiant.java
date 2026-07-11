package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForEquipmentToBattlefieldAndAttachEffect;

import java.util.List;

@CardRegistration(set = "MOR", collectorNumber = "24")
public class StonehewerGiant extends Card {

    public StonehewerGiant() {
        // {1}{W}, {T}: Search your library for an Equipment card, put it onto the battlefield,
        // attach it to a creature you control, then shuffle.
        addActivatedAbility(new ActivatedAbility(
                true,       // requires tap
                "{1}{W}",
                List.of(new SearchLibraryForEquipmentToBattlefieldAndAttachEffect()),
                "{1}{W}, {T}: Search your library for an Equipment card, put it onto the battlefield, attach it to a creature you control, then shuffle."
        ));
    }
}
