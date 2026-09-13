package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "4ED", collectorNumber = "363")
@CardRegistration(set = "ATH", collectorNumber = "77")
@CardRegistration(set = "ATQ", collectorNumber = "82a")
@CardRegistration(set = "ATQ", collectorNumber = "82b")
@CardRegistration(set = "ATQ", collectorNumber = "82c")
@CardRegistration(set = "ATQ", collectorNumber = "82d")
@CardRegistration(set = "V09", collectorNumber = "13")
@CardRegistration(set = "VMA", collectorNumber = "316")
@CardRegistration(set = "EXP", collectorNumber = "43")
public class StripMine extends Card {

    public StripMine() {
        // {T}: Add {C}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));

        // {T}, Sacrifice Strip Mine: Destroy target land.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new SacrificeSelfCost(),
                        new DestroyTargetPermanentEffect(false)
                ),
                "{T}, Sacrifice Strip Mine: Destroy target land.",
                TargetFilters.land()
        ));
    }
}
