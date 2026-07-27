package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import java.util.List;

@CardRegistration(set = "6ED", collectorNumber = "38")
public class ResistanceFighter extends Card {

    public ResistanceFighter() {
        // Sacrifice this creature: Prevent all combat damage target creature would deal this turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new SacrificeSelfCost(), PreventDamageEffect.allCombatByTargetCreatures()),
                "Sacrifice this creature: Prevent all combat damage target creature would deal this turn.",
                TargetFilters.creature()
        ));
    }
}
