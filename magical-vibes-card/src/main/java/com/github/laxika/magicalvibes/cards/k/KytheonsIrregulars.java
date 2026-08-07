package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RenownEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "ORI", collectorNumber = "24")
public class KytheonsIrregulars extends Card {

    public KytheonsIrregulars() {
        // Renown 1
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new RenownEffect(1));

        // {W}{W}: Tap target creature.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{W}{W}",
                List.of(new TapPermanentsEffect(TapUntapScope.TARGET)),
                "{W}{W}: Tap target creature.",
                TargetFilters.creature()
        ));
    }
}
