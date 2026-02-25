package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.SacrificeMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCardTypesToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SOM", collectorNumber = "169")
public class KuldothaForgemaster extends Card {

    public KuldothaForgemaster() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new SacrificeMultiplePermanentsCost(3, new PermanentIsArtifactPredicate()),
                        new SearchLibraryForCardTypesToBattlefieldEffect(Set.of(CardType.ARTIFACT), false, false)
                ),
                "{T}, Sacrifice three artifacts: Search your library for an artifact card, put it onto the battlefield, then shuffle."
        ));
    }
}
