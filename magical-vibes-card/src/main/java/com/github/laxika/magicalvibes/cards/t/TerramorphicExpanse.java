package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCardTypesToBattlefieldEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "10E", collectorNumber = "360")
@CardRegistration(set = "M10", collectorNumber = "229")
@CardRegistration(set = "M11", collectorNumber = "229")
public class TerramorphicExpanse extends Card {

    public TerramorphicExpanse() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new SacrificeSelfCost(),
                        new SearchLibraryForCardTypesToBattlefieldEffect(Set.of(CardType.LAND), true, true)
                ),
                "{T}, Sacrifice Terramorphic Expanse: Search your library for a basic land card, put it onto the battlefield tapped, then shuffle."
        ));
    }
}
