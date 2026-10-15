package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForBasicLandsToBattlefieldTappedAndHandEffect;

import java.util.List;

@CardRegistration(set = "HOB", collectorNumber = "3")
public class TroopOfPonies extends Card {

    public TroopOfPonies() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(
                        new SacrificeSelfCost(),
                        new SearchLibraryForBasicLandsToBattlefieldTappedAndHandEffect()
                ),
                "{2}, {T}, Sacrifice this creature: Search your library for up to two basic land cards, "
                        + "reveal them, put one onto the battlefield tapped and the other into your hand, then shuffle."
        ));
    }
}
