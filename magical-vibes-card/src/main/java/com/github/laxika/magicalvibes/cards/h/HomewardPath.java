package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.EachPlayerGainsControlOfOwnedCreaturesEffect;

import java.util.List;

@CardRegistration(set = "C13", collectorNumber = "295")
public class HomewardPath extends Card {

    public HomewardPath() {
        // {T}: Add {C}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));

        // {T}: Each player gains control of all creatures they own.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new EachPlayerGainsControlOfOwnedCreaturesEffect()),
                "{T}: Each player gains control of all creatures they own."
        ));
    }
}
