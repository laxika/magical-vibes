package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "HOB", collectorNumber = "173")
public class GiantsBoulder extends Card {

    public GiantsBoulder() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ScryEffect(2));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new AwardAnyColorManaEffect()),
                "{1}, {T}: Add one mana of any color."
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{7}",
                List.of(new SacrificeSelfCost(), new DestroyTargetPermanentEffect()),
                "{7}, {T}, Sacrifice this artifact: Destroy target permanent.",
                TargetFilters.permanent()
        ));
    }
}
