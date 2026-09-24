package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.EachPlayerGainsControlOfOwnedCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerGainsControlOfOwnedPermanentsMatchingEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import java.util.List;

@CardRegistration(set = "SLD", collectorNumber = "1467")
@CardRegistration(set = "SLD", collectorNumber = "1851")
@CardRegistration(set = "CMD", collectorNumber = "277")
@CardRegistration(set = "C13", collectorNumber = "295")
public class HomewardPath extends Card {

    public HomewardPath() {
        // {T}: Add {C}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));

        // {T}: Each player gains control of all creatures they own.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new EachPlayerGainsControlOfOwnedPermanentsMatchingEffect(
                        new PermanentIsCreaturePredicate())),
                "{T}: Each player gains control of all creatures they own."
        ));
    }
}
