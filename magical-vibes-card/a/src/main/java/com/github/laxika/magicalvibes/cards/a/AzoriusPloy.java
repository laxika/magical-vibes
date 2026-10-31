package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "DIS", collectorNumber = "106")
public class AzoriusPloy extends Card {

    public AzoriusPloy() {
        setAllowSharedTargets(true);
        target(TargetFilters.creature())
                .addSpellEffect(PreventDamageEffect.allCombatByTargetCreatures());
        target(TargetFilters.creature())
                .addSpellEffect(PreventDamageEffect.allCombatToTargetCreatures());
    }
}
