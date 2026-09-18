package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "C14", collectorNumber = "58")
public class UnstableObelisk extends Card {

    public UnstableObelisk() {
        // {T}: Add {C}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));

        // {7}, {T}, Sacrifice Unstable Obelisk: Destroy target permanent.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{7}",
                List.of(new SacrificeSelfCost(), new DestroyTargetPermanentEffect()),
                "{7}, {T}, Sacrifice Unstable Obelisk: Destroy target permanent.",
                TargetFilters.permanent()
        ));
    }
}
