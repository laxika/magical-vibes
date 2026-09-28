package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.ExileSelfCost;
import com.github.laxika.magicalvibes.model.effect.ShuffleTargetPermanentIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "WHO", collectorNumber = "189")
public class OminousCemetery extends Card {

    public OminousCemetery() {
        // {T}: Add {C}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));

        // {5}, {T}, Exile this land: Target creature's owner shuffles it into their library.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{5}",
                List.of(new ExileSelfCost(), new ShuffleTargetPermanentIntoLibraryEffect()),
                "{5}, {T}, Exile this land: Target creature's owner shuffles it into their library.",
                TargetFilters.creature()
        ));
    }
}
