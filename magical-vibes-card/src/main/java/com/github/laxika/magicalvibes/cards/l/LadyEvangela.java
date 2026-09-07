package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "LEG", collectorNumber = "240")
public class LadyEvangela extends Card {

    public LadyEvangela() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{W}{B}",
                List.of(PreventDamageEffect.allCombatByTargetCreatures()),
                "{W}{B}, {T}: Prevent all combat damage that would be dealt by target creature this turn.",
                TargetFilters.creature()
        ));
    }
}
