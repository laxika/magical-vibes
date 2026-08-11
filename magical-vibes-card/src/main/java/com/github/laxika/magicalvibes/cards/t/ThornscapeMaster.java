package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantProtectionChoiceUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "INV", collectorNumber = "216")
public class ThornscapeMaster extends Card {

    public ThornscapeMaster() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{R}{R}",
                List.of(new DealDamageToTargetCreatureEffect(2)),
                "{R}{R}, {T}: This creature deals 2 damage to target creature.",
                TargetFilters.creature()
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{W}{W}",
                List.of(new GrantProtectionChoiceUntilEndOfTurnEffect()),
                "{W}{W}, {T}: Target creature gains protection from the color of your choice until end of turn.",
                TargetFilters.creature()
        ));
    }
}
