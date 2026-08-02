package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import java.util.List;

@CardRegistration(set = "TMP", collectorNumber = "39")
public class Safeguard extends Card {

    public Safeguard() {
        // {2}{W}: Prevent all combat damage that would be dealt by target creature this turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{W}",
                List.of(PreventDamageEffect.allCombatByTargetCreatures()),
                "{2}{W}: Prevent all combat damage that would be dealt by target creature this turn.",
                TargetFilters.creature()
        ));
    }
}
