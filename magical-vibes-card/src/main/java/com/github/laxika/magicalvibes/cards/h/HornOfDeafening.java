package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "CHR", collectorNumber = "100")
@CardRegistration(set = "LEG", collectorNumber = "280")
public class HornOfDeafening extends Card {

    public HornOfDeafening() {
        // {2}, {T}: Prevent all combat damage that would be dealt by target creature this turn.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(PreventDamageEffect.allCombatByTargetCreatures()),
                "{2}, {T}: Prevent all combat damage that would be dealt by target creature this turn.",
                TargetFilters.creature()
        ));
    }
}
